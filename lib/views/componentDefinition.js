
var getComponentDefinition = require('../get-sbol').getComponentDefinition
var getComponentDefinitionMetadata = require('../get-sbol').getComponentDefinitionMetadata

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var prefixify = require('../prefixify')

var pug = require('pug')

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

            if(req.params.userId) {

		designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
		next()

	    } else {

		designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		uri = config.get('databasePrefix') + 'public/' + designId
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
            'SELECT ?subject ?displayId ?title WHERE {' +
                '   ?subject a sbol2:Collection .' +
                '   ?subject sbol2:member <' + uri + '> .' +
                '   OPTIONAL { ?subject sbol2:displayId ?displayId } .' +
                '   OPTIONAL { ?subject dcterms:title ?title } .' +
                '}'

            sparql(stores, query, (err, results) => {

                if(err) {

                    next(err)

                } else {

                    collections = results.map((result) => {
                        return {
                            uri: result.subject,
                            url: '/'+result.subject.toString().replace(config.get('databasePrefix'),''),
                            name: result.title?result.title:result.displayId
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

                return {
                    uri: type.uri,
		    term: type.uri,
		    description: type.description
                }
		
            })

           meta.roles = meta.roles.map((role) => {

                var igemPrefix = 'http://synbiohub.org/terms/igem/partType/'

                if(!role.term && role.uri.indexOf(igemPrefix) === 0) {

                    return {
                        uri: role.uri,
                        term: role.uri.slice(igemPrefix.length)
                    }

                } else {

                    return {
                        uri: role.uri,
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
	    
	    if (meta.igemReplacedBy.uri != '') {
		meta.igemReplacedBy.uri = '/' + meta.igemReplacedBy.uri.toString().replace(config.get('databasePrefix'),'')
		meta.igemReplacedBy.id = meta.igemReplacedBy.uri.toString().replace('/public/','').replace('/1','') + ' '
		meta.igemReplacedBy.id = meta.igemReplacedBy.id.substring(meta.igemReplacedBy.id.indexOf('/')+1)
	    }

	    meta.url = '/' + meta.uri.toString().replace(config.get('databasePrefix'),'')

            locals.meta = meta

	    locals.components = componentDefinition.components
	    locals.components.forEach((component) => {
		component.link()
		component.url = '/'  + component.definition.uri.toString().replace(config.get('databasePrefix'),'')
		component.typeStr = component.access.toString().replace('http://sbols.org/v2#','')
	    })
	    locals.meta.sequences.forEach((sequence) => {
		sequence.url = '/'  + sequence.uri.toString().replace(config.get('databasePrefix'),'')
	    })


            if(req.params.userId) {
                locals.sbolUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' + meta.id + '.xml'
                locals.fastaUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' + meta.id + '.fasta'
                locals.genBankUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' + meta.id + '.gb'
                locals.searchUsesUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/uses'
                locals.searchTwinsUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/twins'
	    } else {
                locals.sbolUrl = '/public/' + designId + '/' + meta.id + '.xml'
                locals.fastaUrl = '/public/' + designId + '/' + meta.id + '.fasta'
                locals.genBankUrl = '/public/' + designId + '/' + meta.id + '.gb'
                locals.searchUsesUrl = '/public/' + designId + '/uses'
                locals.searchTwinsUrl = '/public/' + designId + '/twins'
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

	    locals.annotations = componentDefinition.annotations;
	    locals.annotations.forEach((annotation) => {
		// var namespaces = listNamespaces(config.get("namespaces")).filter(function(namespace) {
		//     return annotation.name.indexOf(namespace.uri) === 0;
		// });
		// if(namespaces.length != 0) {
		//     var namespace = namespaces.sort((a, b) => a.uri.length - b.uri.length)[0];
		//     var prefixedName = namespace.prefix + ':' + annotation.name.slice(namespace.uri.length);
		//     annotation.name = prefixedName
		// }
		annotation.nameDef = annotation.name
		annotation.name = annotation.name.slice(annotation.name.lastIndexOf('/')+1)
		if (annotation.type === 'uri' && annotation.value.toString().startsWith(config.get('databasePrefix'))) {
		    annotation.uri = '/' + annotation.value.toString().replace(config.get('databasePrefix'),'')
		    annotation.value = annotation.value.substring(0,annotation.value.lastIndexOf('/'))
		    annotation.value = annotation.value.slice(annotation.value.lastIndexOf('/')+1)
		} else {
		    annotation.value = annotation.value.slice(annotation.value.lastIndexOf('/')+1)
		}
	    })
	    locals.annotations = locals.annotations.filter(function(annotation) {
		return (!annotation.name.toString().startsWith('dcterms:created')) && (!annotation.name.toString().startsWith('dcterms:modified')) && (!annotation.name.toString().startsWith('dcterms:creator')) && (!annotation.name.toString().startsWith('dcterms:source')) && (!annotation.name.toString().startsWith('igem:description')) && (!annotation.name.toString().startsWith('igem:notes')) && (!annotation.name.toString().startsWith('igem:status')) && (!annotation.name.toString().startsWith('igem:partStatus')) && (!annotation.name.toString().startsWith('igem:results')) && (!annotation.name.toString().startsWith('igem:category')) && (!annotation.name.toString().startsWith('igem:dominant')) && (!annotation.name.toString().startsWith('igem:favorite')) && (!annotation.name.toString().startsWith('igem:discontinued')) && (!annotation.name.toString().startsWith('igem:replacedBy'))
	    });

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

            res.send(pug.renderFile('templates/views/componentDefinition.jade', locals))
        }
    ], function done(err) {

            res.status(500).send(err.stack)
                
    })
	
};

function listNamespaces(xmlAttribs) {

    var namespaces = [];

    Object.keys(xmlAttribs).forEach(function(attrib) {

        var tokens = attrib.split(':');

        if(tokens[0] === 'xmlns') {

            namespaces.push({
                prefix: tokens[1],
                uri: xmlAttribs[attrib]
            })
        }
    });

    return namespaces;
}



