
var { getVersion } = require('../query/version')

var async = require('async')

var config = require('../config')

var pug = require('pug')

var getUrisFromReq = require('../getUrisFromReq')

var sbol = require('./sbol')

const { fetchSBOLSource } = require('../fetch/fetch-sbol-source')

const fs = require('mz/fs')

module.exports = function (req, res) {
  const { graphUri, uri, designId, url } = getUrisFromReq(req, res)

  getVersion(uri, graphUri).then((result) => {
    var newUri = uri + '/' + result
    fetchSBOLSource(newUri, graphUri).then((tempFilename) => {
      res.status(200).type('application/rdf+xml')
      // .set({ 'Content-Disposition': 'attachment; filename=' + collection.name + '.xml' })

      const readStream = fs.createReadStream(tempFilename)

      readStream.pipe(res).on('finish', () => {
        fs.unlink(tempFilename)
      })
    })
  }).catch((err) => {
    return res.status(404).send(uri + ' not found')
  })
}
