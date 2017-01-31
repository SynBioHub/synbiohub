var getType = require('../get-sbol').getType

var async = require('async')

var config = require('../config')

var sbolCollection = require('./sbolCollection')

var sbolComponentDefinition = require('./sbolComponentDefinition')

var sbolModuleDefinition = require('./sbolModuleDefinition')

var sbolSequence = require('./sbolSequence')

var sbolModel = require('./sbolModel')

var sbolGenericTopLevel = require('./sbolGenericTopLevel')

var config = require('../config')

var pug = require('pug')

module.exports = function(req, res) {

    var stack = require('../stack')()
    var designId
    var uri
    
    async.series([

        function getTopLevel(next) {
	    var stores = []
	    if(req.params.userId) {
		if(req.userStore) stores.push(req.userStore)
		designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
	    } else {
                stores.push(stack.getDefaultStore())
		designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		uri = config.get('databasePrefix') + 'public/' + designId
	    } 

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
			sbolCollection(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#ComponentDefinition') { 
			sbolComponentDefinition(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#ModuleDefinition') { 
			sbolModuleDefinition(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#Sequence') { 
			sbolSequence(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#Model') { 
			sbolModel(req, res)
			return
                    } else {
			sbolGenericTopLevel(req, res)
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


