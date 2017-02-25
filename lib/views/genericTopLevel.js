
var getGenericTopLevel = require('../get-sbol').getGenericTopLevel
var getContainingCollections = require('../get-sbol').getContainingCollections
var filterAnnotations = require('../filterAnnotations')

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
        config: config.get(),
        section: 'component',
        user: req.user
    }

    var meta
    var genericTopLevel
    var collectionIcon 

    var collections = []

    const { graphUris, uri, designId, share, url } = getUrisFromReq(req)

    const graphUri = graphUris[0]
 
    getGenericTopLevel(uri, graphUris).then((result) => {

        genericTopLevel = result.object

        if(!genericTopLevel || genericTopLevel instanceof URI) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' Record Not Found' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }
        meta = sbolmeta.summarizeGenericTopLevel(genericTopLevel)
        if(!meta) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeGenericTopLevel returned null' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

    }).then(function lookupCollections() {

        return getContainingCollections(uri, graphUri, req.url).then((_collections) => {
            collections = _collections
            collections.forEach((collection) => {

                const collectionIcons = config.get('collectionIcons')
                    
                if(collectionIcons[collection.uri])
                    collectionIcon = collectionIcons[collection.uri]
            })
        })

    }).then(function renderView() {

	if (meta.description != '') {
	    meta.description = wiky.process(meta.description, {})
	}

        if (meta.mutableDescription != '') {
            meta.mutableDescriptionSource = meta.mutableDescription
            meta.mutableDescription = wiky.process(meta.mutableDescription, {})
        }

        if (meta.mutableNotes != '') {
            meta.mutableNotesSource = meta.mutableNotes
            meta.mutableNotes = wiky.process(meta.mutableNotes, {})
        }

        if (meta.dcTermsSource != '') {
            meta.dcTermsSourceSource = meta.dcTermsSource
            meta.dcTermsSource = wiky.process(meta.dcTermsSource, {})
        }

        if(req.user) {

            if(req.user.isAdmin) {

                locals.canEdit = true

            } else {

                const ownedBy = genericTopLevel.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')
                const userUri = config.get('databasePrefix') + 'user/' + req.user.username

                if(ownedBy.indexOf(userUri) > -1) {

                    locals.canEdit = true

                } else {

                    locals.canEdit = false

                }

            }

        } else {

            locals.canEdit = false

        }

	meta.url = '/' + meta.uri.toString().replace(config.get('databasePrefix'),'')

        locals.meta = meta

        locals.annotations = filterAnnotations(genericTopLevel.annotations);

        locals.sbolUrl = url + '/' + meta.id + '.xml'
        if(req.params.userId) {
            locals.searchUsesUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/uses'
            locals.searchTwinsUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/twins'
	} else {
            locals.searchUsesUrl = '/public/' + designId + '/uses'
            locals.searchTwinsUrl = '/public/' + designId + '/twins'
	} 

        locals.keywords = []
        locals.citations = []
        locals.prefix = req.params.prefix

        locals.collections = collections

        locals.collectionIcon = collectionIcon

        locals.meta.description = locals.meta.description.split(';').join('<br/>')

        res.send(pug.renderFile('templates/views/genericTopLevel.jade', locals))

    }).catch((err) => {
    
        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err.stack ]
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



