
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const { getContainingCollections } = require('../query/local/collection')

var filterAnnotations = require('../filterAnnotations')
var retrieveCitations = require('../citations')

var loadTemplate = require('../loadTemplate')

var sbolmeta = require('sbolmeta')

var async = require('async')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var wiky = require('../wiky/wiky.js');

var config = require('../config')

var URI = require('sboljs').URI

var getUrisFromReq = require('../getUrisFromReq')

const attachments = require('../attachments')

module.exports = function(req, res) {

	var locals = {
        config: config.get(),
        section: 'model',
        user: req.user
    }

    var meta
    var sbol
    var model
    var collectionIcon

    var collections = []

    var submissionCitations = []
    var citations = []

    const { graphUri, uri, designId, share, url } = getUrisFromReq(req)

    var templateParams = {
        uri: uri
    }

    var getCitationsQuery = loadTemplate('sparql/GetCitations.sparql', templateParams)

    fetchSBOLObjectRecursive('Model', uri, graphUri).then((result) => {

        sbol = result.sbol
        model = result.object

        if(!model || model instanceof URI) {
            locals = {
                config: config.get(),
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
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeModel returned null' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

    }).then(function lookupCollections() {

        return Promise.all([
	    getContainingCollections(uri, graphUri, req.url).then((_collections) => {

		collections = _collections

		collections.forEach((collection) => {

                    const collectionIcons = config.get('collectionIcons')
                    
                    if(collectionIcons[collection.uri])
			collectionIcon = collectionIcons[collection.uri]
		})
            }),

	    sparql.queryJson(getCitationsQuery, graphUri).then((results) => {

                citations = results

            }).then(() => {

                return retrieveCitations(citations).then((resolvedCitations) => {

                    submissionCitations = resolvedCitations;

                    console.log('got citations ' + JSON.stringify(submissionCitations));

                })

            })

        ])

    }).then(function renderView() {

	if (meta.description != '') {
	    meta.description = wiky.process(meta.description, {})
	}

        meta.mutableDescriptionSource = meta.mutableDescription || ''
        if (meta.mutableDescription != '') {
            meta.mutableDescription = wiky.process(meta.mutableDescription, {})
        }

        meta.mutableNotesSource = meta.mutableNotes || ''
        if (meta.mutableNotes != '') {
            meta.mutableNotes = wiky.process(meta.mutableNotes, {})
        }

        meta.sourceSource = meta.source || ''
        if (meta.source != '') {
            meta.source = wiky.process(meta.source, {})
        } 

        meta.attachments = attachments.getAttachmentsFromTopLevel(sbol, model)

	meta.url = '/' + meta.uri.toString().replace(config.get('databasePrefix'),'')

        locals.meta = meta

        locals.meta.triplestore = graphUri ? 'private' : 'public'

        if(req.user) {

            if(req.user.isAdmin) {

                locals.canEdit = true

            } else {

                const ownedBy = model.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')
                const userUri = config.get('databasePrefix') + 'user/' + req.user.username

                if(ownedBy && ownedBy.indexOf(userUri) > -1) {

                    locals.canEdit = true

                } else {

                    locals.canEdit = false

                }

            }

        } else {

            locals.canEdit = false

        }

        locals.annotations = filterAnnotations(model.annotations);

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

        locals.submissionCitations = submissionCitations
	locals.citationsSource = citations.map(function(citation) {
            return citation.citation
        }).join(',');

        locals.meta.description = locals.meta.description.split(';').join('<br/>')

        res.send(pug.renderFile('templates/views/model.jade', locals))

    }).catch((err) => {
        
        locals = {
            config: config.get(),
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


