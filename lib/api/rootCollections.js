
var loadTemplate = require('../loadTemplate')
var sparql = require('../sparql/sparql')

function rootCollections(req, callback) {

    var query = loadTemplate('./sparql/RootCollectionMetadata.sparql', { });

    sparql.queryJson(query, req.params.store).then((sparqlResults) => {

        var results = sparqlResults.map(function(result) {
            return {
                uri: result['Collection'],
                name: result['name'] || '',
                description: result['description'] || '',
                displayId: result['displayId'] || '',
                version: result['version'] || ''
            };
        });

        callback(null, 200, {
            mimeType: 'application/json',
            body: JSON.stringify(results)
        })

    }).catch((err) => {

        callback(err)

    })



}

module.exports = rootCollections

