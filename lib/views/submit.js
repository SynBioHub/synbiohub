var getCollectionMetaData = require('../get-sbol').getCollectionMetaData

var loadTemplate = require('../loadTemplate')

var pug = require('pug')

var retrieveCitations = require('../citations');

var fs = require('fs');

var async = require('async');

var SBOLDocument = require('sboljs')

var extend = require('xtend')

var uuid = require('node-uuid');

var serializeSBOL = require('../serializeSBOL')

var request = require('request')

var config = require('../config')

module.exports = function(req, res) {

    var submissionData = {
        id: req.body.id || '',
        version: req.body.version || '1',
        name: req.body.name || '',
        createdBy: req.user,
        type: req.body.type || req.params.type || 'private',
        acs: req.body.acs || '',
        citations: req.body.citations || '', // comma separated pubmed IDs
        description: req.body.description || '',
        keywords: req.body.keywords || '',
        chassis: req.body.chassis || '',
        purpose: req.body.purpose || '',
        file: req.body.file || ''
    }

    if(req.method === 'POST') {

        submitPost(req, res, submissionData)

    } else {

        submitForm(req, res, submissionData, {})

    }
}

function submitForm(req, res, submissionData, locals) {
	
    var submissionID = '';

	locals = extend({
        section: 'submit',
        user: req.user,
        submission: submissionData,
        errors: []
    }, locals)

    res.send(pug.renderFile('templates/views/submit.jade', locals))
}
	

function submitPost(req, res, submissionData) {

    var stack = require('../stack')()

    var submissionFile = '';
    var submissionCitations = [];
    var submissionSBOL = null

    var errors = []

    submissionData.id = submissionData.id.trim()
    submissionData.version = submissionData.version.trim()
    submissionData.name = submissionData.name.trim()
    submissionData.description = submissionData.description.trim()
    submissionData.purpose = submissionData.purpose.trim()

    if(submissionData.id === '') {
        errors.push('Please enter an id for your submission')
    }

    if(submissionData.version === '') {
        errors.push('Please enter a version for your submission')
    }

    if(submissionData.name === '') {
        errors.push('Please enter a name for your submission')
    }

    if(submissionData.description === '') {
        errors.push('Please enter a brief description for your submission')
    }

//    if(submissionData.purpose === '') {
//        errors.push('Please enter a purpose for your submission')
//    }

    if(!req.file) {
        errors.push('An SBOL file is required')
    }

    if(errors.length > 0) {

        return submitForm(req, res, submissionData, {
            errors: errors
        })
    }

    var convertedSBOL

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

		    return submitForm(req, res, submissionData, {
			errors: errors
		    })

                }
            })

        },

        function lookupCitations(next) {

            console.log('lookupCitations');

            retrieveCitations(submissionData.citations, function(err, citations) {

                submissionCitations = citations;

                console.log('got citations ' + JSON.stringify(submissionCitations));

                next(err);

            });
        },

	function convertSBOL(next) {
            console.log('validating/converting');
	    //console.log(req.file.buffer.toString('utf8'))

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
			  'main_file': req.file.buffer.toString('utf8')
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

        // function convertSBOL(next) {

        //     var converterURL = config.get('converterURL')

        //     request.post({

        //         url: converterURL,
        //         body: req.file.buffer.toString('utf8'),

        //     }, (err, response, body) => {

        //         if(err)
        //             return next(err)

        //         if(response.statusCode >= 300)
        //             return next(new Error(response.statusCode + ' ' + body))

        //         convertedSBOL = body

        //         next()
        //     })
        // },

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

            var keywords = submissionData.keywords.split(',')
                .map((keyword) => keyword.trim())
                .filter((keyword) => keyword !== '')

            var collection = submissionSBOL.collection()

            collection.addStringAnnotation('http://synbiohub.org#uploadedBy', req.user.email)

            keywords.forEach((keyword) => {
                collection.addStringAnnotation('http://synbiohub.org#keyword', keyword)
            })

            submissionCitations.forEach((citation) => {
                collection.addStringAnnotation('http://synbiohub.org#citation', citation)
            })

            collection.addStringAnnotation('http://synbiohub.org#chassis', submissionData.chassis)
            collection.addStringAnnotation('http://synbiohub.org#purpose', submissionData.purpose)

            collection.uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.email) + '/' + submissionData.id + '/' + submissionData.id + '_collection/' + submissionData.version
            collection.persistentIdentity = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.email) + '/' + submissionData.id + '/' + submissionData.id + '_collection'
	    collection.displayId = submissionData.id + '_collection'
	    collection.version = submissionData.version
            collection.name = submissionData.name
            collection.description = submissionData.description

            submissionSBOL.componentDefinitions.forEach((componentDefinition) => {
                collection.addMember(componentDefinition)
            })

            submissionSBOL.moduleDefinitions.forEach((moduleDefinition) => {
                collection.addMember(moduleDefinition)
            })

            submissionSBOL.models.forEach((model) => {
                collection.addMember(model)
            })

            submissionSBOL.sequences.forEach((sequence) => {
                collection.addMember(sequence)
            })

            submissionSBOL.genericTopLevels.forEach((genericTopLevel) => {
                collection.addMember(genericTopLevel)
            })

            submissionSBOL.collections.forEach((subCollection) => {
		if (collection.uri!=subCollection.uri)
                    collection.addMember(subCollection)
            })
	    console.log('about to serialize')
            var xml = serializeSBOL(submissionSBOL)
	    console.log('done serializing')
	    //console.log(xml)

            // 

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
