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

    console.log(req.body)
    
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

    var overwrite_merge = req.body.overwrite_merge

    submissionData.id = submissionData.id.trim()
    submissionData.version = submissionData.version.trim()

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
    var collection
    var sbol

    async.series([

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

	    getCollection(null, uri, [ req.userStore ], function(err, _sbol, _collection) {
	    	if (err) {
	    
		    locals = {
			section: 'errors',
			user: req.user,
			errors: [ uri + ' Not Found' ]
		    }
		    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
		    return        
		} 
		collection = _collection
		sbol = _sbol
		next()
	    })
	}, 

       function retrieveCollectionMetaData(next) {
	   var stores = [
               req.userStore
	   ]

	   var uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.email) + '/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version

           getCollectionMetaData(uri, stores, function(err, _metaData, _storeUrl) {

               if(err) {

		   next()

               } else {
		   if (overwrite_merge === '0') {
		       // Prevent make public
		       console.log('prevent')

		       errors.push('Submission id and version already in use')

		       return cloneForm(req, res, submissionData, {
			   errors: errors
		       })
		   } else if (overwrite_merge === '1') {
		       // Overwrite
		       console.log('overwrite')
		       uriPrefix = uri.substring(0,uri.lastIndexOf('/'))
		       uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/'))
		       
		       var templateParams = {
			   uriPrefix: uriPrefix,
			   version: submissionData.version
		       }

		       var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

		       req.userStore.sparql(removeQuery, (err, result) => {

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
            console.log('validating/converting');
	    xml = serializeSBOL(sbol)
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

        function loadSBOL(next) {

            console.log('loadSBOL');

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
	    submissionSBOL.collections.forEach((collection) => {
		if (collection.uri.toString() === uri.toString()) {
		    newDisplayId = submissionData.id + '_collection'
		    newPersUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.email) + '/' + submissionData.id + '/' + newDisplayId
		    newUri = newPersUri + '/' + submissionData.version
		    collection.uri = newUri
		    collection.displayId = newDisplayId
		    collection.persistentIdentity = newPersUri
		}
	    })
//            collection.persistentIdentity = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.email) + '/' + submissionData.id + '/' + submissionData.id + '_collection'
	//    collection.displayId = submissionData.id + '_collection'

            var xml = serializeSBOL(submissionSBOL)

            var store = req.userStore

            store.upload(xml, function(err, result) {
		console.log('upload')

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
