var pug = require('pug')

var getGenericTopLevel = require('../../lib/get-sbol').getGenericTopLevel

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

module.exports = function(req, res) {

    var stack = require('../../lib/stack')()

	var designId
        var uri

        if(req.params.userId) {
	    designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
	} else  {
	    designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'public/' + designId
	} 

        var stores = [
            stack.getDefaultStore()
        ]

        if(req.userStore)
            stores.push(req.userStore)
	
        getGenericTopLevel(null, uri, stores, function(err, sbol, sequence) {

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

};


