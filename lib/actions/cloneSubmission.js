
const { getCollectionMetaData } = require('../query/collection')
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var loadTemplate = require('../loadTemplate')

var pug = require('pug')

var fs = require('mz/fs');

var async = require('async');

var SBOLDocument = require('sboljs')

var extend = require('xtend')

var serializeSBOL = require('../serializeSBOL')

var request = require('request')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

const cloneSubmission = require('../clone-submission')

var sparql = require('../sparql/sparql')

const tmp = require('tmp-promise')

module.exports = function(req, res) {

    if(req.method === 'POST') {

        var submissionData = {
            id: req.body.id || '',
            version: req.body.version || '',
            overwrite_merge: req.body.overwrite_merge || ''
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

    req.setTimeout(0) // no timeout
	
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

    if (submissionData.id+'_collection' === req.params.displayId &&
	submissionData.version === req.params.version) {
        errors.push('Please enter a different id or version for your submission')
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

    const { designId, uri, graphUri } = getUrisFromReq(req, res)

    fetchSBOLObjectRecursive(uri, req.user.graphUri).then((result) => {

		sbol = result.sbol
		collection = result.object

	}).then(function retrieveCollectionMetaData(next) {

            var newUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version

        return getCollectionMetaData(newUri, graphUri).then((result) => {

            if(!result) {
                return doClone()
            }

            const metaData = result

            if (overwrite_merge === '2' || overwrite_merge === '3') {

                // Merge
                console.log('merge')
                submissionData.name = metaData.name || ''
                submissionData.description = metaData.description || ''

                return doClone()

            } else if (overwrite_merge === '1') {
                
		// Overwrite
                console.log('overwrite')
                uriPrefix = uri.substring(0,uri.lastIndexOf('/'))
                uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/')+1)

                var templateParams = {
		    collection: uri,
                    uriPrefix: uriPrefix,
                    version: submissionData.version
                }
                console.log('removing ' + templateParams.uriPrefix)
                var removeQuery = loadTemplate('sparql/removeCollection.sparql', templateParams)
		return sparql.deleteStaggered(removeQuery, graphUri).then(() => {
			templateParams = {
                            uri: uri
			}
			removeQuery = loadTemplate('sparql/remove.sparql', templateParams)
			sparql.deleteStaggered(removeQuery, graphUri).then(doClone)
		})

            } else {

                // Prevent make public
                console.log('prevent')

                if (req.forceNoHTML || !req.accepts('text/html')) {
                    console.log('prevent')
                    res.status(500).type('text/plain').send('Submission id and version already in use')                 
                    return
                } else {
                    errors.push('Submission id and version already in use')
		    
                    cloneForm(req, res, submissionData, {
                        errors: errors
                    })
                }
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

        function saveTempFile() {

            return tmp.tmpName().then((tmpFilename) => {
                
                return fs.writeFile(tmpFilename, serializeSBOL(sbol)).then(() => {

                    return Promise.resolve(tmpFilename)

                })

            })

        }

    function doClone() {

        console.log('-- validating/converting');

        return saveTempFile().then((tmpFilename) => {

            console.log('tmpFilename is ' + tmpFilename)
            
            return cloneSubmission(tmpFilename, {
                uriPrefix: config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username)+ '/' + submissionData.id + '/',

                version: submissionData.version,

		rootCollectionIdentity: config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection/' + submissionData.version,
		originalCollectionDisplayId: req.params.displayId,
		originalCollectionVersion: req.params.version,
                newRootCollectionDisplayId: submissionData.id + '_collection',
		newRootCollectionVersion: submissionData.version,
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
                return sparql.uploadFile(req.user.graphUri, resultFilename, 'application/rdf+xml').then(() => {
                // TODO: add to collectionChoices
                    res.status(200).type('text/plain').send('Successfully uploaded')
                })
            } else {
                return sparql.uploadFile(req.user.graphUri, resultFilename, 'application/rdf+xml').then((result) => {
		    
                    res.redirect('/manage')
		    
                })
            }

        })
    }
}
