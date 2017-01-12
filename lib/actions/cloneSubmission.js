var getCollectionMetaData = require('../get-sbol').getCollectionMetaData

var getCollection = require('../get-sbol').getCollection

var loadTemplate = require('../loadTemplate')

var pug = require('pug')

var fs = require('fs');

var async = require('async');

var SBOLDocument = require('sboljs')

var extend = require('xtend')

var serializeSBOL = require('../serializeSBOL')

var request = require('request')

var config = require('../config')

module.exports = function(req, res) {
    
    console.log(req)

    if(req.method === 'POST') {

	var submissionData = {
            id: req.body.id || '',
            version: req.body.version || ''
	}

        clonePost(req, res, submissionData)

    } else {

	var submissionData = {
            id: req.params.collectionId || '',
            version: req.params.version || ''
	}

        cloneForm(req, res, submissionData, {})

    }
}

function cloneForm(req, res, submissionData, locals) {
	
    var submissionID = '';

	locals = extend({
        section: 'submit',
        user: req.user,
        submission: submissionData,
        errors: []
    }, locals)

    res.send(pug.renderFile('templates/views/clone.jade', locals))
}
	

function clonePost(req, res, submissionData) {

    var stack = require('../stack')()

    var submissionFile = '';
    var submissionSBOL = null

    var errors = []

    submissionData.id = submissionData.id.trim()
    submissionData.version = submissionData.version.trim()
    console.log(submissionData)

    if(submissionData.id === '') {
        errors.push('Please enter an id for your submission')
    }

    if(submissionData.version === '') {
        errors.push('Please enter a version for your submission')
    }

    if(errors.length > 0) {
        return cloneForm(req, res, submissionData, {
            errors: errors
        })
    }

    var convertedSBOL
    var xml

    async.series([

       function retrieveCollectionMetaData(next) {
	   var stores = [
               stack.getDefaultStore()
	   ]

	    var uri = config.get('databasePrefix') + 'public/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version

            getCollectionMetaData(uri, stores, function(err, _metaData, _storeUrl) {

                if(err) {

		    next()

                } else {

		    errors.push('Submission id and version already in use')

		    return cloneForm(req, res, submissionData, {
			errors: errors
		    })

                }
            })

        },

	function getSBOL(next) {
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
		} 
		xml = serializeSBOL(sbol)
		next()
	    })
	}, 

	function convertSBOL(next) {
            console.log('validating/converting');

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
				      'uri_prefix': config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.email) + '/' + submissionData.id + '/',
				      'version': submissionData.version,
				      'insert_type': false,
				      'main_file_name': 'main file',
				      'diff_file_name': 'comparison file',
				     },
			  'return_file': true,
			  'main_file': xml.toString('utf8')
			}
		}, function(err, response, body) {
		    if(err || response.statusCode >= 300) {
			//console.log(err)
			locals = {
			    section: 'errors',
			    user: req.user,
			    errors: [ err ]
			}
			//console.log(response)
			//console.log(body)
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

        function loadSBOL(next) {

            console.log('loadSBOL');
	    //console.log(convertedSBOL);

            SBOLDocument.loadRDF(convertedSBOL, (err, sbol) => {

                if(err)
                    return next(err)

                submissionSBOL = sbol

                next()

            })

        },

        function saveSubmission(next) {

            console.log('saving...');
	    uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.email) + '/' + submissionData.id + '/' + req.params.displayId + '/' + submissionData.version
	    console.log('looking for ' + uri)
	    submissionSBOL.collections.forEach((collection) => {
		console.log('found ' + collection.uri)
		if (collection.uri.toString() === uri.toString()) {
		    newDisplayId = submissionData.id + '_collection'
		    newPersUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.email) + '/' + submissionData.id + '/' + newDisplayId
		    newUri = newPersUri + '/' + submissionData.version
		    collection.uri = newUri
		    collection.displayId = newDisplayId
		    collection.persistentIdentity = newPersUri
		    console.log('got here ' + collection.uri)
		}
	    })
//            collection.persistentIdentity = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.email) + '/' + submissionData.id + '/' + submissionData.id + '_collection'
	//    collection.displayId = submissionData.id + '_collection'

            var xml = serializeSBOL(submissionSBOL)
	    //console.log(xml)

            var store = req.userStore

            store.upload(xml, function(err, result) {
		console.log('upload')
                console.log(result);

                if(err) {

                    next(err);

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
