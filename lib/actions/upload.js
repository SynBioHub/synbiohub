
const pug = require('pug')

const sparql = require('../sparql/sparql')

const loadTemplate = require('../loadTemplate')

const getUrisFromReq = require('../getUrisFromReq')

const config = require('../config')

const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

const multiparty = require('multiparty')

const uploads = require('../uploads')

const attachments = require('../attachments')

const streamToString = require('stream-to-string')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

const SBOLDocument = require('sboljs')

module.exports = function(req, res) {

    const form = new multiparty.Form()

    const { graphUri, uri, designId, share, url, baseUri } = getUrisFromReq(req)

    form.on('part', (partStream) => {

        if(partStream.filename) {

            uploads.createUpload(partStream).then((uploadInfo) => {

                const { hash, size } = uploadInfo

                return attachments.addAttachmentToTopLevel(
                    graphUri, baseUri, uri, partStream.filename, hash, size,
                    attachments.getTypeFromExtension(partStream.filename), req.user.username)

            }).then(() => {

                return fetchSBOLObjectRecursive('TopLevel', graphUri, uri)

            }).then((result) => {

                const sbol = result.sbol
                const topLevel = result.object
                
                const locals = {
                    config: config.get(),
                    canEdit: true,
                    url: url,
                    attachments: attachments.getAttachmentsFromTopLevel(sbol, topLevel),
                }

                res.send(pug.renderFile('templates/partials/attachments.jade', locals))

            }).catch((err) => {
                res.status(500).send(err.stack)
            })

        }

    })

    form.on('error', (err) => {
        res.status(500).send(err)
    })

    form.parse(req)

}


