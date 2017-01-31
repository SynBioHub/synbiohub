
var constructQuery = require('./constructQuery');

var loadTemplate = require('../loadTemplate')

var sparql = require('../sparql/sparql')

var federate = require('../federation/federate')
var collateMetadata = require('../federation/collateMetadata')

function SearchMetadataEndpoint(type) {

    return federate(search, collateMetadata)

    function search(req, callback) {

        console.log('endpoint: SearchMetadataEndpoint')

        var params

        if(req.body) {
            params = req.body
        } else {
            params = req.query
        }

        var query = loadTemplate('./sparql/SearchMetadata.sparql', {
            type: type,
            criteria: constructQuery(type, params.criteria)
        });

        if(params.offset !== undefined && params.offset !== null)
            query = query + ' OFFSET ' + parseInt(params.offset);

        if(params.limit !== undefined && params.limit !== null)
            query = query + ' LIMIT ' + parseInt(params.limit);

        sparql.queryJson(query, req.params.store, function(err, sparqlResults) {

            if(err)
                return callback(err);

            var results = sparqlResults.map(function(result) {
                return {
                    uri: result[type],
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
        });
    };
}

module.exports = SearchMetadataEndpoint;



