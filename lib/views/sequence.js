
var getSequence = require('../get-sbol').getSequence

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var pug = require('pug')

var sparql = require('../sparql-collate')

var wiky = require('../wiky/wiky.js');

var config = require('../config')

module.exports = function(req, res) {

    var stack = require('../stack')()

	var locals = {
        section: 'component',
        user: req.user
    }

    var prefixes
    var baseUri
    var uri
    var desginId

    var meta
    var sequence

    var collections = []

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

            getSequence(null, uri, stores, function(err, sbol, _sequence) {

                if(err) {

                    next(err)

                } else {

                    sequence = _sequence

                    if(!sequence) {
                        return res.status(404).send('not found\n' + uri)
                    }

                    meta = sbolmeta.summarizeSequence(sequence)
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

        function renderView() {

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

	    meta.url = '/' + meta.uri.toString().replace(config.get('databasePrefix'),'')

	    meta.encoding = sequence.encoding

            locals.meta = meta

	    locals.annotations = sequence.annotations;
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
		return (!annotation.name.toString().startsWith('dcterms'))&&(!annotation.name.toString().startsWith('igem'))
	    });

            if(req.params.userId) {
                locals.sbolUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' + meta.id + '.xml'
                locals.fastaUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' + meta.id + '.fasta'
                locals.searchUsesUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/uses'
                locals.searchTwinsUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/twins'
	    } else {
                locals.sbolUrl = '/public/' + designId + '/' + meta.id + '.xml'
                locals.fastaUrl = '/public/' + designId + '/' + meta.id + '.fasta'
                locals.searchUsesUrl = '/public/' + designId + '/uses'
                locals.searchTwinsUrl = '/public/' + designId + '/twins'
	    } 

            locals.keywords = []
            locals.citations = []
            locals.prefix = req.params.prefix

            locals.collections = collections

            locals.meta.formatted = formatSequence(meta.elements)

	    locals.meta.blastUrl = meta.type === 'AminoAcid' ?
                    'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastp&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome' :
                    'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastn&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome'

            locals.meta.description = locals.meta.description.split(';').join('<br/>')

            res.send(pug.renderFile('templates/views/sequence.jade', locals))
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


