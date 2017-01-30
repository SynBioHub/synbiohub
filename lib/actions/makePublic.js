var getCollectionMetaData = require('../get-sbol').getCollectionMetaData

var pug = require('pug')

var async = require('async');

var request = require('request')

var getCollection = require('../get-sbol').getCollection

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var loadTemplate = require('../loadTemplate')

var extend = require('xtend')

module.exports = function(req, res) {

    var stack = require('../stack')()

    var designId
    var uri
    var overwrite_merge = '0'
    var collectionId = req.params.collectionId
    var version = req.params.version

    if(req.params.userId) {
	designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
    } else {
	designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	uri = config.get('databasePrefix') + 'public/' + designId
    } 

    if(req.method === 'POST') {
	overwrite_merge = req.body.overwrite_merge
        collectionId = req.body.id
        version = req.body.version

	var errors = []
	if(collectionId === '') {
            errors.push('Please enter an id for your submission')
	}

	if(version === '') {
            errors.push('Please enter a version for your submission')
	}

	if(errors.length > 0) {
	    var locals = {}
	    locals = extend({
		section: 'makePublic',
		user: req.user,
                submission: { id: req.params.collectionId || '',
			      version: req.params.version || ''
			    },
		errors: errors
	    }, locals)
	    res.send(pug.renderFile('templates/views/makePublic.jade', locals))
	    return
	}
  
    } 

    console.log('getting collection')
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

	    async.series([

		function retrieveCollectionMetaData(next) {
		    
		    console.log('check if exists already')

		    var stores = [
			stack.getDefaultStore()
		    ]

		    var uri = config.get('databasePrefix') + 'public/' + collection.displayId.replace('_collection','') + '/' + collection.displayId + '/' + collection.version

		    getCollectionMetaData(uri, stores, function(err, _metaData, _storeUrl) {

			if(err) {

			    next()

			} else {
			    if (overwrite_merge === '0') {
				// Prevent make public
				console.log('prevent')
				var locals = {}
				locals = extend({
				    section: 'makePublic',
				    user: req.user,
                                    submission: { id: req.params.collectionId || '',
						  version: req.params.version || ''
						},
				    errors: [ 'Submission id ' + collection.displayId.replace('_collection','') + ' version ' + collection.version + ' already in use' ]
				}, locals)
				res.send(pug.renderFile('templates/views/makePublic.jade', locals))
				return        

			    } else if (overwrite_merge === '1') {
				// Overwrite
				console.log('overwrite')
				uriPrefix = uri.substring(0,uri.lastIndexOf('/'))
				uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/'))

				var templateParams = {
				    uriPrefix: uriPrefix,
				    version: req.params.version
				}

				var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

				stack.getDefaultStore().sparql(removeQuery, (err, result) => {

				    if(err) {

					next(err)
			    
				    } else {

					next()
				    }
				})
				
			    } else {
				// Merge
				console.log('merge')
				collection.name = _metaData[0].name || ''
				collection.description = _metaData[0].description || ''
				next()

			    }
			}
		    })
		},

		function convertSBOL(next) {

		    console.log('convert')
		    var xml = serializeSBOL(sbol)

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
					      'uri_prefix': config.get('databasePrefix') + 'public/' + collectionId + '/',
					      'version': version,
					      'insert_type': false,
					      'main_file_name': 'main file',
					      'diff_file_name': 'comparison file',
					     },
				  'return_file': true,
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
				convertedSBOL = body.result
				next()
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

		    var uriPrefix = uri.substring(0,uri.lastIndexOf('/'))
		    uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/'))

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


