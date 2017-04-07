
const { getCollectionMetaData } = require('../query/collection')

var loadTemplate = require('../loadTemplate')

var pug = require('pug')

var retrieveCitations = require('../citations');

var fs = require('mz/fs');

var async = require('async');

var SBOLDocument = require('sboljs')

var extend = require('xtend')

var uuid = require('node-uuid');

var serializeSBOL = require('../serializeSBOL')

var request = require('request')

var config = require('../config')

var sparql = require('../sparql/sparql')

const prepareSubmission = require('../prepare-submission')

const multiparty = require('multiparty')

const tmp = require('tmp-promise')

var collNS = config.get('databasePrefix') + 'public/'

var apiTokens = require('../apiTokens')

module.exports = function(req, res) {

    if(req.method === 'POST') {

        submitPost(req, res)

    } else {

        submitForm(req, res, {}, {})

    }
}

function submitForm(req, res, submissionData, locals) {

    var collectionQuery = 'PREFIX sbol2: <http://sbols.org/v2#> SELECT DISTINCT ?object WHERE { ?object a sbol2:Collection }'
    var collections

    function sortByNames(a, b) {
        if (a.name < b.name) {
            return -1
        } else {
            return 1
        }
    }

    return sparql.queryJson(collectionQuery, null).then((collections) => {

        collections.forEach((result) => {
            result.uri = result.object
            delete result.object
            result.name = result.uri.toString().replace(collNS,'')
        })
        collections.sort(sortByNames)
	
	submissionData = extend({
            id: '',
            version: '1',
            name: '',
            description: '',
            citations: '', // comma separated pubmed IDs
            collectionChoices: [],
            overwrite_merge: '0',
            //createdBy: req.url==='/remoteSubmit'?JSON.parse(req.body.user):req.user,
            createdBy: req.user,
            file: '',
	}, submissionData)

	locals = extend({
            config: config.get(),
            section: 'submit',
            user: req.user,
            submission: submissionData,
            collections: collections,
            errors: []
	}, locals)

	res.send(pug.renderFile('templates/views/submit.jade', locals))
    })
}
	

function submitPost(req, res) {

    const form = new multiparty.Form()

    form.on('error', (err) => {
        res.status(500).send(err)
    })


    form.parse(req, (err, fields, files) => {

        function getUser() {

            if(req.user) {

                return Promise.resolve(req.user)

            } else {

		console.log('user:'+fields.user[0])
		
                return apiTokens.getUserFromToken(fields.user[0])

            }

        }

        getUser().then((user) => {

            if(err) {
                res.status(500).send(err.stack)
                return
            }

            const submissionData = {
                id: (fields.id[0] || '').trim(),
                version: (fields.version[0] || '1').trim(),
                name: (fields.name[0] || '').trim(),
                description: (fields.description[0] || '').trim(),
                citations: fields.citations[0] || '', // comma separated pubmed IDs
                collectionChoices: fields.collectionChoices || [],
                overwrite_merge: fields.overwrite_merge[0] || '0',
                createdBy: (req.forceNoHTML || !req.accepts('text/html'))?user:req.user
            }

	    console.log('id:' + submissionData.id)
	    console.log('version:' + submissionData.version)
	    console.log('name:' + submissionData.name)
	    console.log('description:' + submissionData.description)
	    console.log('overwrite_merge:' + submissionData.overwrite_merge)
	    console.log('createdBy:' + submissionData.createdBy)

            var errors = []

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

            if(errors.length > 0) {
                if (req.forceNoHTML || !req.accepts('text/html')) {
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
            } else {
                submissionData.citations = []
            }

            var graphUri
            var uri

            graphUri = submissionData.createdBy.graphUri

            uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version

            getCollectionMetaData(uri, graphUri).then((result) => {

                if(!result) {
                    console.log('not found')
		    submissionData.overwrite_merge = '0'
                    return doSubmission()
                }

                const metaData = result.metaData

                if (submissionData.overwrite_merge === '2' || submissionData.overwrite_merge === '3') {

                    // Merge
                    console.log('merge')
                    submissionData.name = metaData.name || ''
                    submissionData.description = metaData.description || ''

                    return doSubmission()

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

                    return sparql.updateQueryJson(removeQuery, graphUri).then(doSubmission)

                } else {

                    // Prevent make public
                    console.log('prevent')

                    if (req.forceNoHTML || !req.accepts('text/html')) {
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

                if (req.forceNoHTML || !req.accepts('text/html')) {
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

            function saveTempFile() {

                if(files.file) {

                     return Promise.resolve(files.file[0].path)

                } else {

                    return tmp.tmpName().then((tmpFilename) => {

                        return fs.writeFile(tmpFilename, fields.file[0]).then(() => {

                            return Promise.resolve(tmpFilename)

                        })

                    })

                }
            }

            function doSubmission() {

                console.log('-- validating/converting');

                return saveTempFile().then((tmpFilename) => {

                    console.log('tmpFilename is ' + tmpFilename)

		    console.log({
			submit: true,
                        uriPrefix: config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/',

                        name: submissionData.name,
                        description: submissionData.description,
                        version: submissionData.version,

			rootCollectionIdentity: config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection/' + submissionData.version,
                        newRootCollectionDisplayId: submissionData.id + '_collection',
			newRootCollectionVersion: submissionData.version,
                        ownedByURI: config.get('databasePrefix') + 'user/' + submissionData.createdBy.username,
                        creatorName: submissionData.createdBy.name,
                        citationPubmedIDs: submissionData.citations,
			collectionChoices: submissionData.collectionChoices,
			overwrite_merge: submissionData.overwrite_merge

                    })
                    
                    return prepareSubmission(tmpFilename, {
			submit: true,
                        uriPrefix: config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/',

                        name: submissionData.name,
                        description: submissionData.description,
                        version: submissionData.version,

			rootCollectionIdentity: config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection/' + submissionData.version,
                        newRootCollectionDisplayId: submissionData.id + '_collection',
			newRootCollectionVersion: submissionData.version,
                        ownedByURI: config.get('databasePrefix') + 'user/' + submissionData.createdBy.username,
                        creatorName: submissionData.createdBy.name,
                        citationPubmedIDs: submissionData.citations,
			collectionChoices: submissionData.collectionChoices,
			overwrite_merge: submissionData.overwrite_merge

                    })

                }).then((result) => {

                    const { success, log, errorLog, resultFilename } = result

                    if(!success) {
                        if (req.forceNoHTML || !req.accepts('text/html')) {
                            res.status(500).type('text/plain').send(errorLog)			
                            return        
                        } else {
                            const locals = {
                                config: config.get(),
                                section: 'invalid',
                                user: req.user,
                                errors: [ errorLog ]
                            }

                            res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
                            return
                        }
                    }

                    console.log('uploading sbol...');

                    if (req.forceNoHTML || !req.accepts('text/html')) {
                        return sparql.uploadFile(submissionData.createdBy.graphUri, resultFilename, 'application/rdf+xml').then(() => {
                // TODO: add to collectionChoices
                            res.status(200).type('text/plain').send('Successfully uploaded')
                        })
                    } else {
                        return sparql.uploadFile(submissionData.createdBy.graphUri, resultFilename, 'application/rdf+xml').then((result) => {

                            res.redirect('/manage')

                        })
                    }

                })

            }
        })
    })
}




