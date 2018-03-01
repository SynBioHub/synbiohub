
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const { getContainingCollections } = require('../query/local/collection')

var filterAnnotations = require('../filterAnnotations')
var retrieveCitations = require('../citations')

const shareImages = require('../shareImages')

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
        meta = sbolmeta.summarizeGenericTopLevel(plan)
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

        if (meta.description != '') {
            meta.description = wiky.process(meta.description, {})
        }

        meta.mutableDescriptionSource = meta.mutableDescription.toString() || ''
        if (meta.mutableDescription.toString() != '') {
            meta.mutableDescription = shareImages(req, meta.mutableDescription.toString())
            meta.mutableDescription = wiky.process(meta.mutableDescription.toString(), {})
        }

        meta.mutableNotesSource = meta.mutableNotes.toString() || ''
        if (meta.mutableNotes.toString() != '') {
            meta.mutableNotes = shareImages(req, meta.mutableNotes.toString())
            meta.mutableNotes = wiky.process(meta.mutableNotes.toString(), {})
        }

        meta.sourceSource = meta.source.toString() || ''
        if (meta.source.toString() != '') {
            meta.source = shareImages(req, meta.source.toString())
            meta.source = wiky.process(meta.source.toString(), {})
        }

        meta.attachments = attachments.getAttachmentsFromTopLevel(sbol, plan,
            req.url.toString().endsWith('/share'))

        locals.canEdit = false

        if (!remote && req.user) {

            const ownedBy = plan.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')
            const userUri = config.get('databasePrefix') + 'user/' + req.user.username

            if (ownedBy && ownedBy.indexOf(userUri) > -1) {

                locals.canEdit = true

            }

        }

        meta.url = '/' + meta.uri.toString().replace(config.get('databasePrefix'), '')
        if (req.url.toString().endsWith('/share')) {
            meta.url += '/' + sha1('synbiohub_' + sha1(meta.uri) + config.get('shareLinkSalt')) + '/share'
        }

        if (plan.wasGeneratedBy) {
            meta.wasGeneratedBy = {
                uri: plan.wasGeneratedBy.uri ? plan.wasGeneratedBy.uri : plan.wasGeneratedBy,
                url: uriToUrl(plan.wasGeneratedBy, req)
            }
        }

        locals.meta = meta

        locals.meta.triplestore = graphUri ? 'private' : 'public'
        locals.meta.remote = remote

        locals.rdfType = {
            name: 'Plan',
            url: 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Plan'
        }

        locals.annotations = filterAnnotations(req, plan.annotations);

        locals.share = share
        locals.sbolUrl = url + '/' + meta.id + '.xml'
        if (req.params.userId) {
            locals.searchUsesUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/uses'
            locals.makePublic = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/makePublic'
            locals.remove = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/remove'
        } else {
            locals.searchUsesUrl = '/public/' + designId + '/uses'
            locals.remove = '/public/' + designId + '/remove'
        }

        locals.keywords = []
        locals.citations = []
        locals.prefix = req.params.prefix

        locals.collections = collections

        locals.collectionIcon = collectionIcon

        locals.submissionCitations = submissionCitations
        locals.citationsSource = citations.map(function (citation) {
            return citation.citation
        }).join(',');

        locals.meta.description = locals.meta.description.split(';').join('<br/>')

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



