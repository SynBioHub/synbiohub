var pug = require('pug')

var getModel = require('../../lib/get-sbol').getModel

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

module.exports = function(req, res) {

    var stack = require('../../lib/stack')()

    stack.getPrefixes((err, prefixes) => {

	var designId
        var uri
        var stores = []

        if(req.params.userId) {
            if(req.userStore) stores.push(req.userStore)
	    designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
	} else  {
            stores.push(stack.getDefaultStore())
	    designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'public/' + designId
	} 
	
        getModel(null, uri, stores, function(err, sbol, sequence) {

            if(err) {

		locals = {
		    section: 'errors',
		    user: req.user,
		    errors: [ err ]
		}
		res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
		return        

            } else {

                res.status(200)
                   .type('application/rdf+xml')
                   .send(serializeSBOL(sbol))
                   //.set({ 'Content-Disposition': 'attachment; filename=' + componentDefinition.name + '.xml' })

            }

        });

    })

};


