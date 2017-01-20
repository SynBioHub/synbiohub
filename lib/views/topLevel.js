var getType = require('../get-sbol').getType

var async = require('async')

var config = require('../config')

var collection = require('./collection')

var componentDefinition = require('./componentDefinition')

var moduleDefinition = require('./moduleDefinition')

var sequence = require('./sequence')

var model = require('./model')

var genericTopLevel = require('./genericTopLevel')

var pug = require('pug')

module.exports = function(req, res) {

    var stack = require('../stack')()
    var designId
    var uri

    async.series([

        function getTopLevel(next) {
	    if(req.params.userId) {
		designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
	    } else {
		designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		uri = config.get('databasePrefix') + 'public/' + designId
	    } 

            var stores = [
                stack.getDefaultStore()
            ]

            if(req.userStore)
                stores.push(req.userStore)

            getType(uri, stores, function(err, result) {

                if(err) {

		    locals = {
			section: 'errors',
			user: req.user,
			errors: [ uri + ' Record Not Found' ]
		    }
		    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
		    return

                } else {

                    if(result[0] && result[0].type==='http://sbols.org/v2#Collection') {
			collection(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#ComponentDefinition') { 
			componentDefinition(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#ModuleDefinition') { 
			moduleDefinition(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#Sequence') { 
			sequence(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#Model') { 
			model(req, res)
			return
                    } else {
			genericTopLevel(req, res)
			return
		    }
		    
		}
            })

        }

    ], function done(err) {

	locals = {
	    section: 'errors',
	    user: req.user,
	    errors: [ err ]
	}
	res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
	return        
        
    })
	
};


