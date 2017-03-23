

var loadTemplate = require('../loadTemplate')
var sparql = require('../sparql/sparql')

var getUrisFromReq = require('../getUrisFromReq')

function subCollections(req, res) {

    const { graphUri, uri, designId, share } = getUrisFromReq(req)

    var query = loadTemplate('./sparql/SubCollectionMetadata.sparql', {

        parentCollection: sparql.escapeIRI(uri)
   
    });

    sparql.queryJson(query, graphUri).then((sparqlResults) => {

        var results = sparqlResults.map(function(result) {
            return {
                uri: result['Collection'],
                name: result['name'] || '',
                description: result['description'] || '',
                displayId: result['displayId'] || '',
                version: result['version'] || ''
            };
        });

        res.header('content-type', 'application/json').send(JSON.stringify(results))

    }).catch((err) => {

        res.status(500).send(err.stack)

    })



}

module.exports = subCollections


