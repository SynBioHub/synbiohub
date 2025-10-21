const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
var config = require('../config')
var getUrisFromReq = require('../getUrisFromReq')
const uploads = require('../uploads')

module.exports = function (req, res) {
  const { graphUri, uri } = getUrisFromReq(req, res)

  fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
    const sbol = result.sbol
    const object = result.object

    var attachmentType = object.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentType')
    var attachmentHash = object.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentHash')

    sbol.attachments.forEach((attachment) => {
      if (uri.toString() === attachment.uri.toString()) {
        attachmentType = attachment.format
        attachmentHash = attachment.hash
      }
    })

    const readStream = uploads.createCompressedReadStream(attachmentHash)
    const mimeType = config.get('attachmentTypeToMimeType')[attachmentType] || 'application/octet-stream'

    res.status(200)
    res.header('Content-Encoding', 'gzip')
    res.header('Content-Disposition', 'attachment; filename="' + object.name + '"')
    res.header('Access-Control-Expose-Headers', 'Content-Disposition')
    res.type(mimeType)
    readStream.pipe(res)
  }).catch((err) => {
    console.error(err.stack)
    return res.status(404).send(err.stack)
  })
}
