
var loadTemplate = require('../loadTemplate')

var federate = require('../federation/federate')
var collateSPARQLResults = require('../federation/collateSPARQLResults')

var sparql = require('../sparql/sparql')

function SPARQLEndpoint(req, callback) {

    console.log('endpoint: SPARQLEndpoint')

    var query
   
    if(req.method === 'POST')
        query = req.body.query
    else
        query = req.query.query

    var accept = req.get('accept')

    console.log('accepting ' + accept)

    console.log(query)

    if(accept) {

        sparql.query(query, req.params.store, accept, function(err, type, result) {

            if(err)
                return callback(err);

            callback(null, 200, {
                mimeType: type,
                body: result
            })

        })

    } else {

        sparql.queryJson(query, req.params.store, function(err, result) {

            if(err)
                return callback(err);

            callback(null, 200, {
                mimeType: 'application/json',
                body: JSON.stringify(result)
            })

        })

    }
}

module.exports = federate(SPARQLEndpoint, collateSPARQLResults)


