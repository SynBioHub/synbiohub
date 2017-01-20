var getType = require('../get-sbol').getType

var async = require('async')

var config = require('../config')

//var fastaCollection = require('./fastaCollection')

var fastaComponentDefinition = require('./fastaComponentDefinition')

//var fastaModule = require('./fastaModule')

var fastaSequence = require('./fastaSequence')

var config = require('../config')

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

                    if(result[0] && result[0].type==='http://sbols.org/v2#ComponentDefinition') { 
			fastaComponentDefinition(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#Sequence') { 
			fastaSequence(req, res)
			return
                    } /*else if(result[0] && result[0].type==='http://sbols.org/v2#Collection') {
			fastaCollection(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#ModuleDefinition') { 
			fastaModuleDefinition(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#Model') { 
			fastaModel(req, res)
			return
                    } else }
			fastaGenericTopLevel(req, res)
			return
                    } */ else {
			locals = {
			    section: 'errors',
			    user: req.user,
			    errors: [ uri + ' is a ' + result[0].type + '.', 
				      'FASTA conversion not supported for this type.' ]
			}
			res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
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
