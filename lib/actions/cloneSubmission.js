
const { getCollectionMetaData } = require('../query/collection')
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var loadTemplate = require('../loadTemplate')

var pug = require('pug')

var fs = require('fs');

var async = require('async');

var SBOLDocument = require('sboljs')

var extend = require('xtend')

var serializeSBOL = require('../serializeSBOL')

var request = require('request')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

var sparql = require('../sparql/sparql')

module.exports = function(req, res) {

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
        config: config.get(),
        section: 'submit',
        user: req.user,
        submission: submissionData,
        errors: []
    }, locals)

    res.send(pug.renderFile('templates/views/clone.jade', locals))
}
	

function clonePost(req, res, submissionData) {

    var submissionFile = '';

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

    const { designId, uri, graphUris } = getUrisFromReq(req)

    fetchSBOLObjectRecursive(uri, req.user.graphUri).then((result) => {

		sbol = result.sbol
		collection = result.object

	}).then(function retrieveCollectionMetaData(next) {

            var newUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version

        return getCollectionMetaData(newUri, graphUris).then((result) => {

            if(!result) {
                return doClone()
            }

            const metaData = result.metaData

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
               uriPrefix = newUri.substring(0,newUri.lastIndexOf('/'))
               uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/')+1)

               var templateParams = {
                   uriPrefix: uriPrefix,
                   version: submissionData.version
               }

               var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

               return req.userStore.sparql(removeQuery).then(doClone)

           } else {
               // Merge
               console.log('merge')
               collection.name = _metaData[0].name || ''
               collection.description = _metaData[0].description || ''
               return doClone()
           }
        })

    }).catch((err) => {
	    
        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err.stack ]
        }

        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    })

    function doClone() {

        console.log('validating/converting');

	    xml = serializeSBOL(sbol)

        return new Promise((resolve, reject) => {
            request({
                method: 'POST',
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
                    'uri_prefix': config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + submissionData.id + '/',
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

                    reject(err || new Error('HTTP ' + response.statusCode))

                } else {
                    if (!body.valid) {

                        locals = {
                            config: config.get(),
                            section: 'invalid',
                            user: req.user,
                            errors: body.errors
                        }
                        res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
                        return
                    }
                    convertedSBOL = body.result
                    resolve(convertedSBOL)
                }
            })

        }).then(function loadSBOL(convertedSBOL) {

            console.log('loadSBOL');

            return new Promise((resolve, reject) => {

                SBOLDocument.loadRDF(convertedSBOL, (err, sbol) => {

                    if(err)
                        reject(err)
                    else
                        resolve(sbol)

                })

            })

        }).then(function saveSubmission(submissionSBOL) {

            console.log('saving...');
	    var newUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + submissionData.id + '/' + req.params.displayId + '/' + submissionData.version
	    submissionSBOL.collections.forEach((collection) => {
		if (collection.uri.toString() === newUri.toString()) {
		    newDisplayId = submissionData.id + '_collection'
		    newPersUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + submissionData.id + '/' + newDisplayId
		    newUri = newPersUri + '/' + submissionData.version
		    collection.uri = newUri
		    collection.displayId = newDisplayId
		    collection.persistentIdentity = newPersUri
		}
	    })
//            collection.persistentIdentity = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection'
	//    collection.displayId = submissionData.id + '_collection'

            var xml = serializeSBOL(submissionSBOL)

            return sparql.upload(req.user.graphUri, xml, 'application/rdf+xml').then(() => {
                res.redirect('/manage');
            })
        })

    }

}
