
var sbolRdfToXml = require('../sbolRdfToXml');
var constructQuery = require('./constructQuery');
var sbolToSparql = require('../sparql/sbolToSparql')
var SBOLDocument = require('sboljs');

var loadTemplate = require('../loadTemplate')

var sparql = require('../sparql/sparql')

var federate = require('../federation/federate')
var collateSBOL = require('../federation/collateSBOL')

function SearchSBOLComponentSummaryEndpoint() {

    return federate(search, collateSBOL)

    function search(req, callback) {

        console.log('endpoint: SearchSBOLComponentSummaryEndpoint')
        
        SBOLDocument.loadRDF(req.body.sbol, function(err, sbol) {
            
            if(err) {
                callback(err)
                return
            }

            var query = loadTemplate('./sparql/SearchSBOLComponentSummary.sparql', {           
                criteria: sbolToSparql(sbol),
                limit: parseInt(req.body.limit),
                offset: parseInt(req.body.offset)
            });
    
            sparql.query(query, req.params.store, 'application/rdf+xml', function(err, type, rdf) {

                if(err)
                    return callback(err);

                callback(null, 200, {
                    mimeType: type,
                    body: rdf
                })
            })
            
        })
    }
}

module.exports = SearchSBOLComponentSummaryEndpoint;



