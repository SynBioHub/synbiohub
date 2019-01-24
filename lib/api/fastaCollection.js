
var pug = require('pug')

const { fetchCollectionFASTA } = require('../fetch/fetch-collection-fasta')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function (req, res) {
  const { graphUri, uri, designId, share } = getUrisFromReq(req, res)

  return fetchCollectionFASTA(uri).then((fasta) => {
    res.send(fasta)
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}
