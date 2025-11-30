
var getUrisFromReq = require('../getUrisFromReq')

const sparql = require('../sparql/sparql')

var loadTemplate = require('../loadTemplate')

function metadata (req, res) {
  const { graphUri, uri } = getUrisFromReq(req, res)

  var templateParams = {
    uri: uri
  }

  var metadataQuery = loadTemplate('sparql/GetTopLevelMetadata.sparql', templateParams)

  sparql.queryJson(metadataQuery, graphUri).then((results) => {
    res.header('Content-Disposition', 'attachment; filename="' + req.params.displayId + '.json"')
    res.header('Access-Control-Expose-Headers', 'Content-Disposition')
    res.status(200).type('application/json')
    res.header('content-type', 'application/json').send(JSON.stringify(results))
  }).catch((err) => {
    console.error(err.stack)
    res.status(404).send(err.stack)
  })
}

module.exports = metadata
