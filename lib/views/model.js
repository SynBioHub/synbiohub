
var getModel = require('../get-sbol').getModel
var getContainingCollections = require('../get-sbol').getContainingCollections

var sbolmeta = require('sbolmeta')

var async = require('async')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var wiky = require('../wiky/wiky.js');

var config = require('../config')

var URI = require('urijs')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	var locals = {
        section: 'model',
        user: req.user
    }

    var meta
    var model

    var collections = []

	const { graphUris, uri, designId } = getUrisFromReq(req)

    const graphUri = graphUris[0]

    getModel(uri, graphUris).then((result) => {

        model = result.object

        if(!model || model instanceof URI) {
            locals = {
                section: 'errors',
                user: req.user,
                errors: [ uri + ' Record Not Found' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

        meta = sbolmeta.summarizeModel(model)
        if(!meta) {
            locals = {
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeModel returned null' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

    }).then(function lookupCollections() {

        return getContainingCollections(uri, graphUri).then((_collections) => {
            collections = _collections
        })

    }).then(function renderView() {

	    if (meta.description != '') {
		meta.description = wiky.process(meta.description, {})
	    }

	    if (meta.igemDescription != '') {
		meta.igemDescription = wiky.process(meta.igemDescription, {})
	    }

	    if (meta.igemNotes != '') {
		meta.igemNotes = wiky.process(meta.igemNotes, {})
	    }

	    if (meta.dcTermsSource != '') {
		meta.dcTermsSource = wiky.process(meta.dcTermsSource, {})
	    }

	    meta.url = '/' + meta.uri.toString().replace(config.get('databasePrefix'),'')

            locals.meta = meta

	    locals.annotations = model.annotations;
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
		} else if (annotation.type === 'uri') {
		    annotation.uri = annotation.value.toString()
		    annotation.value = annotation.value.slice(annotation.value.lastIndexOf('/')+1)
		}
	    })
	    locals.annotations = locals.annotations.filter(function(annotation) {
		return (!annotation.name.toString().startsWith('created')) && (!annotation.name.toString().startsWith('modified')) && (!annotation.name.toString().startsWith('creator')) && (!annotation.name.toString().startsWith('source')) && (!annotation.name.toString().startsWith('description')) && (!annotation.name.toString().startsWith('notes')) && (!annotation.name.toString().startsWith('status')) && (!annotation.name.toString().startsWith('partStatus')) && (!annotation.name.toString().startsWith('results')) && (!annotation.name.toString().startsWith('category')) && (!annotation.name.toString().startsWith('dominant')) && (!annotation.name.toString().startsWith('favorite')) && (!annotation.name.toString().startsWith('discontinued')) && (!annotation.name.toString().startsWith('replacedBy'))
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

        locals.meta.description = locals.meta.description.split(';').join('<br/>')

        res.send(pug.renderFile('templates/views/model.jade', locals))

    }).catch((err) => {
        
        locals = {
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
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


