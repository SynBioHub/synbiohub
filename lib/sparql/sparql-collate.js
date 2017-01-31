
var async = require('async')

var sparql = require('./sparql')

function sparql(stores, query, callback) {

    var collatedResults = []

    async.eachSeries(stores, (store, next) => {

        sparql.queryJson(query, store, (err, results) => {

            if(err) {

                callback(err)

            } else {

                [].push.apply(collatedResults, results)

                next()
            }
        })

    }, (err) => {

        callback(null, collatedResults)
                    
    })

}

module.exports = sparql



