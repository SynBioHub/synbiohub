
const pug = require('pug')

const sparql = require('../sparql/sparql')

const loadTemplate = require('../loadTemplate')

const getUrisFromReq = require('../getUrisFromReq')

const config = require('../config')

const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

const multiparty = require('multiparty')

const uploads = require('../uploads')

module.exports = function(req, res) {

    const uri = req.body.uri
    const userUri = config.get('databasePrefix') + 'user/' + req.user.username

    const graphUri = getGraphUriFromTopLevelUri(uri, userUri)

    const form = new multiparty.Form()

    form.on('part', (partStream) => {

        if(partStream.filename) {

            const fileExtension = partStream.filename.slice(
                partStream.filename.lastIndexOf('.'))

            uploads.createUpload(partStream).then((hash) => {

                const query = loadTemplate('sparql/AttachUpload.sparql', {
                    topLevel: uri,
                    attachmentURI: auri,
                    name: JSON.stringify(name),
                    description: JSON.stringify(description),
                    hash: JSON.stringify(hash),
                    type: typeuri
                })

                sparql.queryJson(query).then((res) => {

                })

            }).catch((err) => {
                res.status(500).send(err)
            })

        }

    })

    form.on('error', (err) => {
        res.status(500).send(err)
    })

    form.parse(req)

}


