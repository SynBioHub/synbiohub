
const getUrisFromReq = require('../getUrisFromReq')
const splitUri = require('../splitUri')

const { fetchSBOLSource } = require('../fetch/fetch-sbol-source')

const prepareSnapshot = require('../conversion/prepare-snapshot')

const fs = require('mz/fs')

const sparql = require('../sparql/sparql')

module.exports = function (req, res) {
  const { uri, graphUri, baseUri } = getUrisFromReq(req, res)
  const { displayId } = splitUri(uri)

  return fetchSBOLSource(uri, graphUri).then((tempFilename) => {
    return prepareSnapshot(tempFilename, {
      version: new Date().getTime() + '_snapshot',
      uriPrefix: baseUri
    }).then((result) => {
      const { resultFilename } = result

      return sparql.uploadFile(null, resultFilename, 'application/rdf+xml').then(() => {
        res.redirect('/admin/remotes')
      })
    })
  })
}
