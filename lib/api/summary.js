var pug = require('pug')

var getComponentDefinition = require('../../lib/get-sbol').getComponentDefinition

var sbolmeta = require('sbolmeta')

var config = require('../config')

exports = module.exports = function(req, res) {

    var stack = require('../../lib/stack')()

    stack.getPrefixes((err, prefixes) => {

	var designId
	var uri
	var stores = []
    
	if(req.params.userId) {
	    if (req.userStore) stores.push(req.userStore)
	    designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
	} else {
	    stores.push(stack.getDefaultStore())
	    designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'public/' + designId
	} 

        getComponentDefinition(null, uri, stores, function(err, sbol, componentDefinition) {

            if(err) {
           
		locals = {
                    section: 'errors',
                    user: req.user,
                    errors: [ uri + ' Not Found' ]
		}
		res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
		return        

            } else {

                if(!componentDefinition) {
                    return res.status(404).send('not found\n' + uri)
                }

                try {
                res.status(200)
                   .type('application/json')
                   .send(JSON.stringify(sbolmeta.summarizeDocument(sbol), null, 2))
                }catch(e) {
                res.status(500).send(e.stack)
                }

            }

        });

    })

};


