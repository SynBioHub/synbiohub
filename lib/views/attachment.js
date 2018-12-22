
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const { getContainingCollections } = require('../query/collection')

var retrieveCitations = require('../citations')

var sbolmeta = require('./utils/sbolmeta')

var async = require('async')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var config = require('../config')

var URI = require('sboljs').URI

var getUrisFromReq = require('../getUrisFromReq')

const uriToUrl = require('../uriToUrl')

var sha1 = require('sha1');

module.exports = function (req, res) {

    var locals = {
        config: config.get(),
        section: 'attachment',
        user: req.user
    }

    var meta
    var attachment
    var collectionIcon
    var remote

    var collections = []

    var submissionCitations = []

    const { graphUri, uri, designId, share, url } = getUrisFromReq(req, res)

    var getCitationsQuery =
        'PREFIX sbol2: <http://sbols.org/v2#>\n' +
        'PREFIX purl: <http://purl.obolibrary.org/obo/>\n' +
        'SELECT\n' +
        '    ?citation\n' +
        'WHERE {\n' +
        '    <' + uri + '> purl:OBI_0001617 ?citation\n' +
        '}\n'

    fetchSBOLObjectRecursive(uri, graphUri).then((result) => {

        sbol = result.sbol
        attachment = result.object
        remote = result.remote

        if (!attachment || attachment instanceof URI) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [uri + ' Record Not Found']
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }
        meta = sbolmeta.summarizeGenericTopLevel(attachment,req,sbol,remote,graphUri)
        if (!meta) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [uri + ' summarizeGenericTopLevel returned null']
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
            name: 'Attachment',
            url: 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Attachment'
        }

        locals.share = share
        locals.sbolUrl = url + '/' + meta.id + '.xml'
        locals.prefix = req.params.prefix

        locals.collections = collections

        locals.collectionIcon = collectionIcon

        locals.submissionCitations = submissionCitations

        locals.attachmentType = attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentType')
        locals.attachmentHash = attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentHash')
        locals.attachmentDownloadURL = url + '/download'
        locals.size = attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentSize')

        locals.attachmentIsImage = locals.attachmentType === 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#imageAttachment'

        res.send(pug.renderFile('templates/views/attachment.jade', locals))

    }).catch((err) => {

        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [err.stack],
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



