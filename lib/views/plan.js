
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const { getContainingCollections } = require('../query/local/collection')

var retrieveCitations = require('../citations')

const shareImages = require('../shareImages')

var loadTemplate = require('../loadTemplate')

var sbolmeta = require('./utils/sbolmeta')

var async = require('async')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var wiky = require('../wiky/wiky.js');

var config = require('../config')

var URI = require('sboljs').URI

var getUrisFromReq = require('../getUrisFromReq')

const uriToUrl = require('../uriToUrl')

var sha1 = require('sha1');

module.exports = function (req, res) {

    var locals = {
        config: config.get(),
        section: 'component',
        user: req.user
    }

    var meta
    var sbol
    var plan
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
        plan = result.object
        remote = result.remote

        if (!plan || plan instanceof URI) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [uri + ' Record Not Found: ' + plan]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return Promise.reject()
        }
        meta = sbolmeta.summarizeGenericTopLevel(plan,req,sbol,remote,graphUri)
        if (!meta) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [uri + ' summarizeGenericTopLevel returned null']
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return Promise.reject()
        }

    }).then(function lookupCollections() {

        return Promise.all([
            getContainingCollections(uri, graphUri, req.url).then((_collections) => {

                collections = _collections

                collections.forEach((collection) => {

                    collection.url = uriToUrl(collection.uri)

                    const collectionIcons = config.get('collectionIcons')

                    if (collectionIcons[collection.uri])
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

        locals.meta = meta

        locals.rdfType = {
            name: 'Plan',
            url: 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Plan'
        }

        locals.share = share
        locals.sbolUrl = url + '/' + meta.id + '.xml'
        locals.prefix = req.params.prefix

        locals.collections = collections

        locals.collectionIcon = collectionIcon

        locals.submissionCitations = submissionCitations
        locals.citationsSource = citations.map(function (citation) {
            return citation.citation
        }).join(',');

        res.send(pug.renderFile('templates/views/plan.jade', locals))

    }).catch((err) => {

        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [err.stack]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    })

};

function listNamespaces(xmlAttribs) {

    var namespaces = [];

    Object.keys(xmlAttribs).forEach(function (attrib) {

        var tokens = attrib.split(':');

        if (tokens[0] === 'xmlns') {

            namespaces.push({
                prefix: tokens[1],
                uri: xmlAttribs[attrib]
            })
        }
    });

    return namespaces;
}



