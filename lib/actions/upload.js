
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

module.exports = function(req, res) {

    const form = new multiparty.Form()

    const { graphUris, uri, designId, share, url, baseUri } = getUrisFromReq(req)
    const graphUri = graphUris[0]

    form.on('part', (partStream) => {

        if(partStream.filename) {

            uploads.createUpload(partStream).then((hash) => {

                return attachments.addAttachmentToTopLevel(
                    graphUri, baseUri, uri, partStream.filename, hash,
                        attachments.getTypeFromExtension(partStream.filename))

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


