
var getComponentDefinition = require('../../lib/get-sbol').getComponentDefinition

var base64 = require('../base64')

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

module.exports = function(req, res) {

    var stack = require('../../lib/stack')()

    stack.getPrefixes((err, prefixes) => {

        var baseUri
        var uri

        if(req.params.designURI) {
            uri = base64.decode(req.params.designURI)
        } else if(req.params.userId) {
	    designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
	} else if (req.params.collectionId) {
	    var designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'public/' + designId
	} else {
            baseUri = prefixes[req.params.prefix]
            uri = baseUri + req.params.designid
        }

        var stores = [
            stack.getDefaultStore()
        ]

        if(req.userStore)
            stores.push(req.userStore)
	
        getComponentDefinition(null, uri, stores, function(err, sbol, componentDefinition) {

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


