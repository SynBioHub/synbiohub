var getType = require('../get-sbol').getType

var async = require('async')

var config = require('../config')

var sbolCollection = require('./sbolCollection')

var sbolComponent = require('./sbolComponent')

var sbolModule = require('./sbolModule')

var sbolSequence = require('./sbolSequence')

var config = require('../config')

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
	    console.log(uri)

            var stores = [
                stack.getDefaultStore()
            ]

            if(req.userStore)
                stores.push(req.userStore)

            getType(uri, stores, function(err, result) {
		console.log('err='+err)
		console.log('result[0].type='+result[0].type)

                if(err) {

                    res.status(500).send(err)

                } else {

                    if(result[0] && result[0].type==='http://sbols.org/v2#Collection') {
			sbolCollection(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#ComponentDefinition') { 
			sbolComponent(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#ModuleDefinition') { 
			sbolModule(req, res)
			return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#Sequence') { 
			sbolSequence(req, res)
			return
                    } else {
			res.status(500).send(err)
		    }

		}
            })

        }

    ], function done(err) {

            res.status(500).send(err)
                
    })
	
};


