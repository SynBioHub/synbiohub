
var getUrisFromReq = require('../getUrisFromReq')

const sparql = require('../sparql/sparql')

var loadTemplate = require('../loadTemplate')

function metadata(req, res) {

    const { graphUri, uri, designId, share } = getUrisFromReq(req, res)

    var templateParams = {
        uri: uri
    }

    var metadataQuery = loadTemplate('sparql/GetTopLevelMetadata.sparql', templateParams)

    sparql.queryJson(metadataQuery, graphUri).then((results) => {

        res.header('content-type', 'application/json').send(JSON.stringify(results))

    }).catch((err) => {

        res.status(500).send(err.stack)

    })

}

module.exports = metadata


