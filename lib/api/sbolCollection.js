
var stack = require('../../lib/stack');

var getCollection = require('../../lib/get-sbol').getCollection

var base64 = require('../base64')

module.exports = function(req, res) {

    stack.getPrefixes((err, prefixes) => {

        var baseUri
        var uri

        if(req.params.collectionURI) {
            uri = base64.decode(req.params.collectionURI)
        } else {
            baseUri = prefixes[req.params.prefix]
            uri = baseUri + req.params.designid
        }

        getCollection(null, uri, [ stack.getDefaultStore(), req.userStore ], function(err, sbol, collection) {

            if(err) {

                res.status(500).send(err)

            } else {

                res.status(200)
                   .type('application/rdf+xml')
                   .send(sbol.serializeXML({
                       'xmlns:sybio': 'http://www.sybio.ncl.ac.uk#',
                       'xmlns:rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
                       'xmlns:ncbi': 'http://www.ncbi.nlm.nih.gov#'
                   }))
                   //.set({ 'Content-Disposition': 'attachment; filename=' + componentDefinition.name + '.xml' })

            }

        });

    })

};


