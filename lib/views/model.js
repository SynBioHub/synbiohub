
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const { getContainingCollections } = require('../query/local/collection')

var retrieveCitations = require('../citations')

var loadTemplate = require('../loadTemplate')

var sbolmeta = require('./utils/sbolmeta')

var async = require('async')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var config = require('../config')

var URI = require('sboljs').URI

var getUrisFromReq = require('../getUrisFromReq')

const uriToUrl = require('../uriToUrl')

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
    var remote

    var collections = []

    var submissionCitations = []
    var citations = []

    const { graphUri, uri, designId, share, url } = getUrisFromReq(req, res)

    var templateParams = {
        uri: uri
    }

    var getCitationsQuery = loadTemplate('sparql/GetCitations.sparql', templateParams)

    fetchSBOLObjectRecursive('Model', uri, graphUri).then((result) => {

        sbol = result.sbol
        model = result.object
	remote = result.remote || false

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

        meta = sbolmeta.summarizeModel(model,req,sbol,remote,graphUri)

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

                    collection.url = uriToUrl(collection.uri)

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

                    //console.log('got citations ' + JSON.stringify(submissionCitations));

                })

            })

        ])

    }).then(function renderView() {

	if (meta.modelSource.toString().startsWith(config.get('databasePrefix'))) {
	    meta.modelSource = '/' + meta.modelSource.toString().replace(config.get('databasePrefix'),'')
	    meta.modelSourceName = 'Attachment'
	} else {
	    meta.modelSourceName = meta.modelSource
	}
								  
        locals.meta = meta

	locals.rdfType = {
	    name : 'Model',
	    url : 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Model'
	}

	locals.share = share
        locals.sbolUrl = url + '/' + meta.id + '.xml'
        locals.prefix = req.params.prefix

        locals.collections = collections
        locals.collectionIcon = collectionIcon

        locals.submissionCitations = submissionCitations
	locals.citationsSource = citations.map(function(citation) {
            return citation.citation
        }).join(',');

        res.send(pug.renderFile('templates/views/model.jade', locals))

    }).catch((err) => {
        console.log(err)
        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    })
	
};
