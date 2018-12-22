
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

var sha1 = require('sha1');

module.exports = function(req, res) {

	var locals = {
        config: config.get(),
        section: 'component',
        user: req.user
    }

    var meta
    var sbol
    var implementation
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
 
    fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
	
        sbol = result.sbol
        implementation = result.object
        remote = result.remote

        if(!implementation || implementation instanceof URI) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' Record Not Found: ' + implementation ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return Promise.reject()
        }
        meta = sbolmeta.summarizeGenericTopLevel(implementation,req,sbol,remote,graphUri)
        if(!meta) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeGenericTopLevel returned null' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return Promise.reject()
        }  


      }).then(function lookupTests() {

        var templateParams = {
            uri: sparql.escapeIRI(uri)
        }

        var query = loadTemplate('sparql/GetTests.sparql', templateParams)

        return sparql.queryJson(query, graphUri).then((results) => {

            builds = results.map((result) => {
                return result.impl
            })

        })


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

	meta.built = { url : implementation.built.uri.toString(),
		       name : implementation.built.name
		     }

        locals.meta = meta

	locals.rdfType = {
	    name : 'Implementation',
	    url : 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Implementation'
	}

	locals.share = share
            locals.createTest = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/createTest'
        locals.sbolUrl = url + '/' + meta.id + '.xml'
        locals.prefix = req.params.prefix

        locals.collections = collections

        locals.collectionIcon = collectionIcon

        locals.submissionCitations = submissionCitations
	locals.citationsSource = citations.map(function(citation) {
            return citation.citation
        }).join(',');

        res.send(pug.renderFile('templates/views/implementation.jade', locals))

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



