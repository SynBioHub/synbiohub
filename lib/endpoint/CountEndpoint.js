var loadTemplate = require('../loadTemplate')

var sparql = require('../sparql/sparql')

var federate = require('../federation/federate')

var collateCounts = require('../federation/collateCounts')

function CountEndpoint(type) {

    return federate(endpoint, collateCounts)

    function endpoint(req, callback) {

	var query = loadTemplate('./sparql/Count.sparql', {
            type: type
	})

	console.log('endpoint: CountEndpoint')

	sparql.queryJson(query, req.params.store).then((sparqlResults) => {

            callback(null, 200, {
		mimeType: 'text/plain',
		body: sparqlResults[0].count.toString()
            })
	    
	}).catch((err) => {

            callback(err)

	})
    }
}

module.exports = CountEndpoint
