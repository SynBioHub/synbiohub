
var stack = require('./stack')

var async = require('async')

function sparql(stores, query, callback) {

    var collatedResults = []

    async.eachSeries(stores, (store, next) => {

        store.sparql(query, (err, results) => {

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



