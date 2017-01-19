var getCollectionMetaData = require('../get-sbol').getCollectionMetaData

var pug = require('pug')

var async = require('async');

var request = require('request')

var getCollection = require('../get-sbol').getCollection

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var loadTemplate = require('../loadTemplate')

module.exports = function(req, res) {

    var stack = require('../stack')()

    var designId
    var uri

    if(req.params.userId) {
	designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
    } else {
	designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	uri = config.get('databasePrefix') + 'public/' + designId
    } 
    console.log(uri)

    getCollection(null, uri, [ req.userStore ], function(err, sbol, collection) {

	if (err) {
	    
	    locals = {
		section: 'errors',
		user: req.user,
		errors: [ uri + ' Not Found' ]
	    }
	    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
	    return        

	} else {

            var xml = serializeSBOL(sbol)

	    async.series([

		function retrieveCollectionMetaData(next) {
		    
		    console.log('get meta')

		    var stores = [
			stack.getDefaultStore()
		    ]

		    var uri = config.get('databasePrefix') + 'public/' + collection.displayId.replace('_collection','') + '/' + collection.displayId + '/' + collection.version

		    getCollectionMetaData(uri, stores, function(err, _metaData, _storeUrl) {

			if(err) {

			    next()

			} else {
	    
			    locals = {
				section: 'errors',
				user: req.user,
				errors: [ 'Submission id ' + collection.displayId.replace('_collection','') + ' version ' + collection.version + ' already in use' ]
			    }
			    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
			    return        

			}
		    })
		    
		},

		function convertSBOL(next) {

		    console.log('convert')

		    request(
			{ method: 'POST',
			  uri: config.get('converterURL'),
			  'content-type': 'application/json',
			  json: { 'options': {'language' : 'SBOL2',
					      'test_equality': false,
					      'check_uri_compliance': config.get('requireCompliant'),
					      'check_completeness': config.get('requireComplete'),
					      'check_best_practices': config.get('requireBestPractice'),
					      'continue_after_first_error': false,
					      'provide_detailed_stack_trace': false,
					      'subset_uri': '',
					      'uri_prefix': config.get('databasePrefix') + 'public/' + collection.displayId.replace('_collection','') + '/',
					      'version': '',
					      'insert_type': false,
					      'main_file_name': 'main file',
					      'diff_file_name': 'comparison file',
					     },
				  'return_file': false,
				  'main_file': xml
				}
			}, function(err, response, body) {
			    if(err || response.statusCode >= 300) {
	    
				locals = {
				    section: 'errors',
				    user: req.user,
				    errors: [ err ]
				}
				res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
				return        

			    } else {
				if (!body.valid) {
				    locals = {
					section: 'invalid',
					user: req.user,
					errors: body.errors
				    }
				    res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
				    return
				}
				request({
				    method: 'GET',
 	   			    uri: body.output_file.replace(':5000',''),
				},
					function(err, response, body) {
					    if(err || response.statusCode >= 300) {
	    
						locals = {
						    section: 'errors',
						    user: req.user,
						    errors: [ err ]
						}
						res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
						return        

					    }
					    convertedSBOL = body
					    next()
					}
				       )	
			    }
			});
		},
	
		function upload(next) {
		    
		    console.log('upload')

		    stack.getDefaultStore().upload(convertedSBOL, (err, result) => {

			if(err) {
			    
			    next(err)
			    
			} else {
			    
			    next()
			}
			
		    })
		},

		function removeSubmission(next) {

		    console.log('remove')

		    var designId
		    var uri
		    
		    if(req.params.userId) {
			designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
			uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
		    } else {
			designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
			uri = config.get('databasePrefix') + 'public/' + designId
		    } 
		    uri = uri.substring(0,uri.lastIndexOf('/'))
		    uri = uri.substring(0,uri.lastIndexOf('/'))

		    var templateParams = {
			uriPrefix: uri,
			version: req.params.version
		    }

		    var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

		    req.userStore.sparql(removeQuery, (err, result) => {

			if(err) {

			    next(err)
			    
			} else {
			    
			    res.redirect('/manage');
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
	}
    })

};


