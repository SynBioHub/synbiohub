
var constructQuery = require('./constructQuery');
var sbolToSparql = require('../sparql/sbolToSparql')
var SBOLDocument = require('sboljs');

var loadTemplate = require('../loadTemplate')

var sparql = require('../sparql/sparql')

var federate = require('../federation/federate')
var collateCounts = require('../federation/collateCounts')

function SearchSBOLComponentSummaryCountEndpoint() {

    return federate(search, collateCounts)

    function search(req, callback) {

        console.log('endpoint: SearchSBOLComponentSummaryCountEndpoint')

        SBOLDocument.loadRDF(req.body.sbol, function(err, sbol) {
            
            if(err) {
                callback(err)
                return
            }
            
            var query = loadTemplate('./sparql/SearchSBOLComponentSummaryCount.sparql', {           
                criteria: sbolToSparql(sbol),
            })

            sparql.queryJson(query, req.params.store, function(err, result) {

                console.log(result)

                if(err) {
                    callback(err)
                    return
                }

                else if (result && result.length > 0 && result[0].count)
                {
                    callback(null, 200, {
                        mimeType: 'text/plain',
                        body: result[0].count + ''
                    })
                }   
                else
                {
                    callback(null, 200, {
                        mimeType: 'text/plain',
                        body: '0'
                    })
                }
            })
        })
    }
}

module.exports = SearchSBOLComponentSummaryCountEndpoint;



