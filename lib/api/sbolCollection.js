
var stack = require('../../lib/stack');

var getCollection = require('../../lib/get-sbol').getCollection

var base64 = require('../base64')

var serializeSBOL = require('../serializeSBOL')

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

        var stores = [
            stack.getDefaultStore()
        ]

        if(req.userStore)
            stores.push(req.userStore)

        getCollection(null, uri, stores, function(err, sbol, collection) {

            if(err) {

                res.status(500).send(err)

            } else {

                res.status(200)
                   .type('application/rdf+xml')
                   .send(serializeSBOL(sbol))
                   //.set({ 'Content-Disposition': 'attachment; filename=' + componentDefinition.name + '.xml' })

            }

        });

    })

};


