

var collateMetadata = require('../federation/collateMetadata')

var loadTemplate = require('../loadTemplate')
var sparql = require('../sparql/sparql')

var federate = require('../federation/federate')

var getUrisFromReq = require('../getUrisFromReq')

function subCollections(req, callback) {

    const { graphUris, uri, designId, share } = getUrisFromReq(req)

    var query = loadTemplate('./sparql/SubCollectionMetadata.sparql', {

        parentCollection: sparql.escapeIRI(uri)
   
    });

    sparql.queryJson(query, graphUris[0]).then((sparqlResults) => {

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

module.exports = federate(subCollections, collateMetadata)
