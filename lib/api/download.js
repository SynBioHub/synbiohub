var pug = require('pug')

var getGenericTopLevel = require('../../lib/get-sbol').getGenericTopLevel

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

const uploads = require('../uploads')

module.exports = function(req, res) {

    const { graphUris, uri, designId, share } = getUrisFromReq(req)

    getGenericTopLevel(uri, graphUris).then((result) => {

        const sbol = result.sbol
        const object = result.object

        const attachmentType = object.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentType')
        const attachmentHash = object.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentHash')

        const readStream = uploads.createCompressedReadStream(attachmentHash)

        const mimeType = config.get('attachmentTypeToMimeType')[attachmentType] || 'application/octet-stream'

        res.status(200)
        res.header('Content-Encoding', 'gzip')
        res.type(mimeType)
        readStream.pipe(res)

    }).catch((err) => {

        return res.status(500).send(err.stack)

    })

}


