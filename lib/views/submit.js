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

var sparql = require('../sparql/sparql')

module.exports = function(req, res) {

    var submissionData = {
        id: req.body.id || '',
        version: req.body.version || '1',
        name: req.body.name || '',
        description: req.body.description || '',
        citations: req.body.citations || '', // comma separated pubmed IDs
        keywords: req.body.keywords || '',
	overwrite_merge: req.body.overwrite_merge || '0',
        createdBy: req.url==='/remoteSubmit'?JSON.parse(req.body.user):req.user,
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
        config: config.get(),
        section: 'submit',
        user: req.user,
        submission: submissionData,
        errors: []
    }, locals)

    res.send(pug.renderFile('templates/views/submit.jade', locals))
}
	

function submitPost(req, res, submissionData) {

    var errors = []

    submissionData.id = submissionData.id.trim()
    submissionData.version = submissionData.version.trim()
    submissionData.name = submissionData.name.trim()
    submissionData.description = submissionData.description.trim()

    if(submissionData.createdBy === undefined) {
        errors.push('Must be logged in to submit')
    }

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

    if (req.url==='/remoteSubmit') {
	if(!req.body.file) {
            errors.push('An SBOL file is required')
	}
    } else {
	if(!req.file) {
            errors.push('An SBOL file is required')
	}
    }

    if(errors.length > 0) {
	if (req.url==='/remoteSubmit') {
            res.status(500).type('text/plain').send(errors)			
            return
	} else {
            return submitForm(req, res, submissionData, {
		errors: errors
            })
	}
    }

    if(submissionData.citations) {
        submissionData.citations = submissionData.citations.split(',').map(function(pubmedID) {
            return pubmedID.trim();
        }).filter(function(pubmedID) {
            return pubmedID !== '';
        });
    }

    var graphUris
    var uri

    graphUris = [
        submissionData.createdBy.graphUri
    ]

    uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version

    getCollectionMetaData(uri, graphUris).then((result) => {

        if(!result) {
            console.log('not found')
            return doSubmission()
        }

        const graphUri = result.graphUri
        const metaData = result.metaData
        
        if (submissionData.overwrite_merge === '2') {

            // Merge
            console.log('merge')
            submissionData.name = metaData.name || ''
            submissionData.description = metaData.description || ''

            return doSubmissionNew()

        } else if (submissionData.overwrite_merge === '1') {
            // Overwrite
            console.log('overwrite')
            uriPrefix = uri.substring(0,uri.lastIndexOf('/'))
            uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/')+1)

            var templateParams = {
                uriPrefix: uriPrefix,
                version: submissionData.version
            }
            console.log('removing ' + templateParams.uriPrefix)
            var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)
            console.log(removeQuery)

            return sparql.queryJson(removeQuery, graphUri).then(doSubmission)

        } else {

            // Prevent make public
            console.log('prevent')

	    if (req.url==='/remoteSubmit') {
		console.log('prevent')
		res.status(500).type('text/plain').send('Submission id and version already in use')                 
		return
	    } else {
		errors.push('Submission id and version already in use')

		submitForm(req, res, submissionData, {
                    errors: errors
		})
	    }
        }

    }).catch((err) => {
	if (req.url==='/remoteSubmit') {
            res.status(500).type('text/plain').send(err.stack)
	} else {
            const locals = {
                config: config.get(),
		section: 'errors',
		user: req.user,
		errors: [ err.stack ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
	}
    })

    function doSubmissionNew() {
	var xml = req.url==='/remoteSubmit'?req.body.file.toString('utf8'):req.file.buffer.toString('utf8')
	console.log(xml)
	if (req.url==='/remoteSubmit') {
	    res.status(200).type('text/plain').send('Successfully uploaded')
	} else {
	    res.redirect('/manage')
	}
    }

    function doSubmission() {

        console.log('validating/converting');

        return new Promise((resolve, reject) => {
	    console.log(req.url==='/remoteSubmit'?req.body.file.toString('utf8'):req.file.buffer.toString('utf8'))
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
                    'uri_prefix': config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/',
                    'version': submissionData.version,
                    'insert_type': false,
                    'main_file_name': 'main file',
                    'diff_file_name': 'comparison file',
                },
                    'return_file': true,
  		    'main_file': req.url==='/remoteSubmit'?req.body.file.toString('utf8'):req.file.buffer.toString('utf8')
                }
            }, function(err, response, body) {

                if(err || response.statusCode >= 300) {
		    if (req.url==='/remoteSubmit') {
			res.status(500).type('text/plain').send(err)			
			return        
		    } else {
			reject(err || new Error('HTTP ' + response.statusCode))
		    }
                } else {

                    if(!body.valid) {
			if (req.url==='/remoteSubmit') {
                            res.status(500).type('text/plain').send(body.errors)			
                            return
			} else {
                            const locals = {
                                config: config.get(),
				section: 'invalid',
				user: req.user,
				errors: body.errors
                            }

                            res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
			}
                    } else {

                        const convertedSBOL = body.result

                        resolve(convertedSBOL)

                    }
                }
            })

        }).then(function loadSBOL(convertedSBOL) {

            return new Promise((resolve, reject) => {

                SBOLDocument.loadRDF(convertedSBOL, (err, sbol) => {

                    if(err) {
                        reject(err)
                        return
                    }

                    resolve(sbol)

                })

            })

        }).then(function saveSubmission(submissionSBOL) {

            console.log('saving...');

            var keywords = submissionData.keywords.split(',')
                .map((keyword) => keyword.trim())
                .filter((keyword) => keyword !== '')

            var collection = submissionSBOL.collection()

            collection.addUriAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy', 
					config.get('databasePrefix') + 'user/' + submissionData.createdBy.username)
            collection.addStringAnnotation('http://purl.org/dc/elements/1.1/creator', submissionData.createdBy.name);

            var date = new Date();
            collection.addDateAnnotation('http://purl.org/dc/terms/created', date.toISOString())

            keywords.forEach((keyword) => {
                collection.addStringAnnotation('http://synbiohub.org#keyword', keyword)
            })

            if (submissionData.citations != "") {
                submissionData.citations.forEach((citation) => {
                    console.log('adding citation ' + citation)
                    collection.addStringAnnotation('http://purl.obolibrary.org/obo/OBI_0001617', citation)
                })
            }

            collection.uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection/' + submissionData.version
            collection.persistentIdentity = config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection'
            collection.displayId = submissionData.id + '_collection'
            collection.version = submissionData.version
            collection.name = submissionData.name
            collection.description = submissionData.description

            submissionSBOL.componentDefinitions.forEach((componentDefinition) => {
		// check if CD exists (either in private store with this URI, or some public store with original URI)
		// if does not exist, then upload the CD
		// if does exist, ideally should check if it is the same
		// if exists and this is not the same, need to either replace or add with new version
		// Same procedure for all TopLevels except Collections (see below)

		//var sbol = new SBOLDocument()
		//sbol._componentDefinitions.push(componentDefinition)
		//var xmlCD = serializeSBOL(sbol)
		//console.log(xmlCD)
		componentDefinition.addUriAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy', 
					config.get('databasePrefix') + 'user/' + submissionData.createdBy.username)
                collection.addMember(componentDefinition)
            })

            submissionSBOL.moduleDefinitions.forEach((moduleDefinition) => {
		moduleDefinition.addUriAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy', 
					config.get('databasePrefix') + 'user/' + submissionData.createdBy.username)
                collection.addMember(moduleDefinition)
            })

            submissionSBOL.models.forEach((model) => {
		model.addUriAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy', 
					config.get('databasePrefix') + 'user/' + submissionData.createdBy.username)
                collection.addMember(model)
            })

            submissionSBOL.sequences.forEach((sequence) => {
		sequence.addUriAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy', 
					config.get('databasePrefix') + 'user/' + submissionData.createdBy.username)
                collection.addMember(sequence)
            })

            submissionSBOL.genericTopLevels.forEach((genericTopLevel) => {
		genericTopLevel.addUriAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy', 
						 config.get('databasePrefix') + 'user/' + submissionData.createdBy.username)
                collection.addMember(genericTopLevel)
            })

            submissionSBOL.collections.forEach((subCollection) => {
		// Collections are a little different, the member fields can be merged, but all other fields should be left 
		// unchanged
                if (collection.uri!=subCollection.uri) {
		    collection.addUriAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy', 
						 config.get('databasePrefix') + 'user/' + submissionData.createdBy.username)
                    collection.addMember(subCollection)
		}
            })

            console.log('about to serialize')
            var xml = serializeSBOL(submissionSBOL)
            console.log('done serializing')

	    if (req.url==='/remoteSubmit') {
		return sparql.upload(submissionData.createdBy.graphUri, xml, 'application/rdf+xml').then(() => {
                    res.status(200).type('text/plain').send('Successfully uploaded')
		})
	    } else {
		return sparql.upload(submissionData.createdBy.graphUri, xml, 'application/rdf+xml').then((result) => {

                    res.redirect('/manage')

		})
	    }

        })
    }
}