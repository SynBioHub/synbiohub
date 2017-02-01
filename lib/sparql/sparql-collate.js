
var async = require('async')

var sparql = require('./sparql')

function sparql(graphUris, query, callback) {

    return Promise.all(graphUris.map(
                (graphUri) => sparql.queryJson(query, graphUri)))
                  .then(collateResults)

    function collateResults(results) {

        var collatedResults = []

        results.forEach((resultSet) => {

            [].push.apply(collatedResults, resultSet)

        })

        return Promise.resolve(collatedResults)

    }

}

module.exports = sparql



