var pug = require('pug')

var getComponentDefinition = require('../../lib/get-sbol').getComponentDefinition

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

module.exports = function(req, res) {

	var designId
        var uri

        if(req.params.userId) {
	    designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
	} else  {
	    designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'public/' + designId
	} 

        var graphUris = [
            null
        ]

        if(req.user && req.user.graphUri)
            graphUris.push(req.user.graphUri)
	
        getComponentDefinition(uri, graphUris, function(err, sbol, componentDefinition) {

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


