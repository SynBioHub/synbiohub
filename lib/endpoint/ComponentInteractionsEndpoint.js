
var SBOLDocument = require('sboljs')

var getSBOL = require('../getSBOL')

var sbolRdfToXml = require('../sbolRdfToXml')

var sparql = require('../sparql/sparql')

var config = require('../config')

var federate = require('../federation/federate')
var collateSBOL = require('../federation/collateSBOL')

var loadTemplate = require('../loadTemplate')

const getRdfSerializeAttribs = require('../getRdfSerializeAttribs')

function ComponentInteractionsEndpoint(type) {

    return federate(endpoint, collateSBOL)

    function endpoint(req, callback) {

        console.log('endpoint: ComponentInteractionsEndpoint')

        var uri

        if(req.params.prefix) {

            var prefixes = config.get('prefixes')

            var baseUri = prefixes[req.params.prefix]

            if(!baseUri) {
                return callback(new Error('unknown prefix: ' + req.params.prefix))
            }

            uri = baseUri + req.params.uri

        } else {

            uri = req.params.uri

        }

        var sbol = new SBOLDocument()

        var query = loadTemplate('./sparql/ComponentInteractions.sparql', {           
            uri: uri
        });

        console.log(query)

        sparql.queryJson(query, req.params.store, function(err, type, results) {

            if(err)
                return callback(err);

            console.log(results)

            var interactionURIs = []
           
            results.forEach((result) => {

                interactionURIs.push(result.module)
                interactionURIs.push(result.interaction)

            })

            getSBOL(sbol, req.params.store, interactionURIs, (err, sbol) => {

                if(err)
                    return callback(err)

                callback(null, 200, {
                    mimeType: 'application/rdf+xml',
                    body: sbol.serializeXML(getRdfSerializeAttribs())
                })
            })
        })

    }
}

module.exports = ComponentInteractionsEndpoint


