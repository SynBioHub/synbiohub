
var pug = require('pug')

const { fetchSBOLSource } = require('../fetch/fetch-sbol-source')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

const fs = require('mz/fs')

module.exports = function (req, res) {
  req.setTimeout(0) // no timeout

  const { graphUri, uri } = getUrisFromReq(req, res)

  fetchSBOLSource(uri, graphUri).then((tempFilename) => {
    res.status(200).type('application/rdf+xml')
    res.header('Content-Disposition', 'attachment; filename="' + req.params.displayId + '.xml"')

    const readStream = fs.createReadStream(tempFilename)

    readStream.pipe(res).on('finish', () => {
      fs.unlink(tempFilename)
    })
  }).catch((err) => {
    if (!req.accepts('text/html')) {
      return res.status(404).send(uri + ' not found')
    } else {
      var locals = {
        config: config.get(),
        section: 'errors',
        user: req.user,
        errors: [ err.stack ]
      }
      res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    }
  })
}
