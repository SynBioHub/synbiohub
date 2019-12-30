
var getUrisFromReq = require('../getUrisFromReq')

const { getSubCollections } = require('../query/collection')

function subCollections (req, res) {
  const { graphUri, uri } = getUrisFromReq(req, res)

  getSubCollections(uri, graphUri).then((collections) => {
    res.header('content-type', 'application/json').send(JSON.stringify(collections))
  }).catch((err) => {
    console.error(err.stack)
    res.status(500).send(err.stack)
  })
}

module.exports = subCollections
