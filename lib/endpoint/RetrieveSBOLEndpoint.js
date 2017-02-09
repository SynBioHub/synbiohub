
var SBOLDocument = require('sboljs')

var getSBOL = require('../getSBOL')

var sbolRdfToXml = require('../sbolRdfToXml')

var config = require('../config')

var federate = require('../federation/federate')
var collateSBOL = require('../federation/collateSBOL')

const getRdfSerializeAttribs = require('../getRdfSerializeAttribs')

function SBOLEndpoint(type) {

    return federate(endpoint, collateSBOL)

    function endpoint(req, callback) {

        console.log('endpoint: RetrieveSBOLEndpoint')

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

        getSBOL(new SBOLDocument(), type, req.params.store, [ uri ]).then((sbol) => {

            callback(null, 200, {
                mimeType: 'application/rdf+xml',
                body: sbol.serializeXML(getRdfSerializeAttribs())
            })

        }).catch((err) => {

            callback(err)
            
        })
    }
}

module.exports = SBOLEndpoint


