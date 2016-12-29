
var getComponentDefinition = require('../get-sbol').getComponentDefinition
var getComponentDefinitionMetadata = require('../get-sbol').getComponentDefinitionMetadata

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var prefixify = require('../prefixify')

var pug = require('pug')

var base64 = require('../base64')

var sparql = require('../sparql-collate')

var getDisplayList = require('../getDisplayList')

var wiky = require('../wiky/wiky.js');

var config = require('../config')

module.exports = function(req, res) {

    var stack = require('../stack')()

	var locals = {
        section: 'component',
        user: req.user
    }

    // todo check the prefix is real
    //req.params.prefix
    //req.params.designid

    var prefixes
    var baseUri
    var uri
    var desginId

    var meta
    var componentDefinition

    var encodedProteins = []
    var collections = []

    var otherComponents = []
    var mappings = {}

    var stores = [
        stack.getDefaultStore()
    ]

    if(req.userStore)
        stores.push(req.userStore)

    async.series([

        function getPrefixes(next) {

            if(req.params.prefix) {

                stack.getPrefixes((err, p) => {

                    prefixes = p
                    baseUri = prefixes[req.params.prefix]
                    uri = baseUri + req.params.designid

                    next(err)
                })

            } else if(req.params.userId) {

		designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
		next()

	    } else if(req.params.collectionId) {

		designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		uri = config.get('databasePrefix') + 'public/' + designId
		next()

	    } else {

                uri = base64.decode(req.params.designURI)

                next()

            }

        },

        function retrieveSBOL(next) {

            getComponentDefinition(null, uri, stores, function(err, sbol, _componentDefinition) {

                if(err) {

                    next(err)

                } else {

                    componentDefinition = _componentDefinition

                    if(!componentDefinition) {
                        return res.status(404).send('not found\n' + uri)
                    }

                    meta = sbolmeta.summarizeComponentDefinition(componentDefinition)
                    next()
                }
            })

        },

        function lookupEncodedProteins(next) {

            var query =
                'PREFIX sybio: <http://www.sybio.ncl.ac.uk#>\n' +
            'SELECT ?subject WHERE {' +
                '   ?subject sybio:en_by <' + uri + '>' +
                '}'

            sparql(stores, query, (err, results) => {

                if(err) {

                    next(err)

                } else {

                    console.log(results)

                    encodedProteins = results.map((result) => {
                        return result.subject
                    })

                    next()

                }
            })

        },

        function lookupCollections(next) {

            var query =
                'PREFIX sbol2: <http://sbols.org/v2#>\n' +
                'PREFIX dcterms: <http://purl.org/dc/terms/>\n' +
            'SELECT ?subject ?title WHERE {' +
                '   ?subject a sbol2:Collection .' +
                '   ?subject sbol2:member <' + uri + '> .' +
                '   ?subject dcterms:title ?title .' +
                '}'

            sparql(stores, query, (err, results) => {

                if(err) {

                    next(err)

                } else {

                    collections = results.map((result) => {
                        return {
                            uri: result.subject,
                            //url: '/collection/' + base64.encode(result.subject),
                            url: '/search/collection/' + base64.encode(result.subject),
                            name: result.title
                        }
                    })

                    next()

                }
            })

        },

        function getOtherComponentMetaData(next) {

            if(meta.protein && meta.protein.encodedBy)
                otherComponents = otherComponents.concat(meta.protein.encodedBy)
                        /* todo and subcomponents */

            otherComponents = otherComponents.concat(encodedProteins)

            async.map(otherComponents, (uri, next) => {

    //<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:prov="http://www.w3.org/ns/prov#" xmlns:sbol="http://sbols.org/v2#" xmlns:sybio="http://www.sybio.ncl.ac.uk#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:ncbi="http://www.ncbi.nlm.nih.gov#">

                getComponentDefinitionMetadata(null, uri, stores, (err, metadata) => {

                    console.log('md for ' + uri)
                    console.log(metadata)

                    next(null, {
                        uri: uri,
                        name: metadata[0].name
                    })

                })

            }, (err, componentNameMappings) => {

                if(err) {

                    next(err)

                } else {

                    componentNameMappings.forEach((mapping) => {
                        mappings[mapping.uri] = mapping.name
                    })

                    next()
                     
                }
            })

        },

        function renderView() {

            meta.types = meta.types.map((type) => {

                var soPrefix = 'http://identifiers.org/so/'

                if(!type.term && type.uri.indexOf(soPrefix) === 0) {

                    return {
                        uri: '/search/type/' + base64.encode(type.uri),
                        term: 'igem:' + type.uri.slice(soPrefix.length)
                    }

                } else {

                    return {
                        uri: '/search/type/' + base64.encode(type.uri),
			term: type.uri,
			description: type.description
                    }

                }
		
            })

           meta.roles = meta.roles.map((role) => {

                var igemPrefix = 'http://synbiohub.org/terms/igem/partType/'

                if(!role.term && role.uri.indexOf(igemPrefix) === 0) {

                    return {
                        uri: '/search/role/' + base64.encode(role.uri),
                        term: 'igem:' + role.uri.slice(igemPrefix.length)
                    }

                } else {

                    return {
                        uri: '/search/role/' + base64.encode(role.uri),
			term: role.uri,
			description: role.description
                    }

                }

            })

	    if (meta.description != '') {
		meta.description = wiky.process(meta.description, {})
	    }

	    if (meta.igemDescription != '') {
		meta.igemDescription = wiky.process(meta.igemDescription, {})
	    }

	    if (meta.igemNotes != '') {
		meta.igemNotes = wiky.process(meta.igemNotes, {})
	    }

	    if (meta.source != '') {
		meta.source = wiky.process(meta.source, {})
	    }

            locals.meta = meta

            if(req.params.designid) {
                locals.sbolUrl = '/component/' + base64.encode(uri) + '.xml'
                locals.fastaUrl = '/component/' + base64.encode(uri) + '.fasta'
                locals.genBankUrl = '/component/' + base64.encode(uri) + '.gb'
                locals.searchUsesUrl = '/search/uses/' + base64.encode(uri)
                locals.searchTwinsUrl = '/search/twins/' + base64.encode(uri)
            } else if(req.params.userId) {
                locals.sbolUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '.xml'
                locals.fastaUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '.fasta'
                locals.genBankUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '.gb'
                locals.searchUsesUrl = '/search/uses/' + base64.encode(uri)
                locals.searchTwinsUrl = '/search/twins/' + base64.encode(uri)
	    } else if(req.params.collectionId) {
                locals.sbolUrl = '/public/' + designId + '.xml'
                locals.fastaUrl = '/public/' + designId + '.fasta'
                locals.genBankUrl = '/public/' + designId + '.gb'
                locals.searchUsesUrl = '/search/uses/' + base64.encode(uri)
                locals.searchTwinsUrl = '/search/twins/' + base64.encode(uri)
	    } else {
                locals.sbolUrl = '/component/' + req.params.designURI + '.xml'
                locals.fastaUrl = '/component/' + req.params.designURI + '.fasta'
                locals.genBankUrl = '/component/' + req.params.designURI + '.gb'
                locals.searchUsesUrl = '/search/uses/' + req.params.designURI
                locals.searchTwinsUrl = '/search/twins/' + req.params.designURI
            }

            locals.keywords = []
            locals.citations = []
            locals.prefix = req.params.prefix

            if(meta.uri.indexOf('http://parts.igem.org/') === 0) {

                meta.experience = meta.uri + ':Experience'

            } 

            locals.encodedProteins = encodedProteins.map((uri) => {

                var prefixified = prefixify(uri, prefixes)

                return {
                    uri: uri,
                    name: mappings[uri],
                    url: '/entry/' + prefixified.prefix + '/' + prefixified.uri
                }

            })

            locals.collections = collections

            locals.meta.sequences.forEach((sequence) => {

                sequence.formatted = formatSequence(sequence.elements)

                sequence.blastUrl = sequence.type === 'AminoAcid' ?
                    'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastp&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome' :
                    'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastn&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome'

            })

            locals.meta.description = locals.meta.description.split(';').join('<br/>')

            if(locals.meta.protein) {

                if(locals.meta.protein.encodedBy) {

                    locals.meta.protein.encodedBy = locals.meta.protein.encodedBy.map((uri) => {

                        var prefixified = prefixify(uri, prefixes)

                        return {
                            uri: uri,
                            name: mappings[uri],
                            url: '/entry/' + prefixified.prefix + '/' + prefixified.uri
                        }

                    })

                }
            }

            locals.meta.displayList = getDisplayList(componentDefinition)

            res.send(pug.renderFile('templates/views/component.jade', locals))
        }
    ])
	
};


