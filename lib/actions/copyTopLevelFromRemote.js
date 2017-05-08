var { getCollectionMetaData }  = require('../query/collection')

var pug = require('pug')

var async = require('async');

var request = require('request')

var { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var loadTemplate = require('../loadTemplate')

var extend = require('xtend')

var getUrisFromReq = require('../getUrisFromReq')

var sparql = require('../sparql/sparql')

const tmp = require('tmp-promise')

var fs = require('mz/fs');

const prepareSubmission = require('../prepare-submission')

module.exports = function(req, res) {

    var overwrite_merge = '0'
    var collectionId = req.params.collectionId
    var version = req.params.version
    var name
    var description

    const { graphUri, uri, designId } = getUrisFromReq(req)

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
		config: config.get(),
		section: 'copyTopLevelFromRemote',
		user: req.user,
                submission: { id: req.params.collectionId || '',
			      version: version || ''
			    },
		errors: errors
	    }, locals)
	    res.send(pug.renderFile('templates/views/makePublic.jade', locals))
	    return
	}
  
    } 

    console.log('getting collection')

    var sbol
    var collection
    var newRootCollectionDisplayId = collectionId + '_collection'

    console.log('uri:'+uri)
    console.log('graphUri:'+req.user.graphUri)

    fetchSBOLObjectRecursive(uri, req.user.graphUri).then((result) => {

        sbol = result.sbol
        topLevel = result.object

	if (version==='current') version = '1'

        var graphUri = req.user.graphUri

        var uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + collectionId + '/' + collectionId + '_collection' + '/' + version

	console.log('check if exists already')

        return getCollectionMetaData(uri, graphUri).then((result) => {

            if(!result) {

                /* not found */
		overwrite_merge = '0'
                return copyTopLevelFromRemote()

            }

            const metaData = result
        
            if (overwrite_merge === '0') {
                // Prevent make public
                console.log('prevent')
                var locals = {}
                locals = extend({
                    config: config.get(),
                    section: 'copyTopLevelFromRemote',
                    user: req.user,
                    submission: { id: req.params.collectionId || '',
                        version: version || ''
                    },
                    errors: [ 'Submission id ' + collectionId + ' version ' + version + ' already in use' ]
                }, locals)
                res.send(pug.renderFile('templates/views/makePublic.jade', locals))

            } else if (overwrite_merge === '1') {
                // Overwrite
                console.log('overwrite')
                uriPrefix = uri.substring(0,uri.lastIndexOf('/'))
                uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/')+1)

                var templateParams = {
                    uriPrefix: uriPrefix,
                    version: version
                }

                var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

                return sparql.deleteStaggered(removeQuery, null).then(copyTopLevelFromRemote())

            } else {
                // Merge
                console.log('merge')
                name = metaData.name || ''
                description = metaData.description || ''

                return copyTopLevelFromRemote()

            }
        })

    }).catch((err) => {

        const locals = {
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

    function copyTopLevelFromRemote() {

            console.log('-- validating/converting');

            return saveTempFile().then((tmpFilename) => {

                console.log('tmpFilename is ' + tmpFilename)
                
                return prepareSubmission(tmpFilename, {
		    copy: true,
                    uriPrefix: config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + collectionId + '/',

                    name: '',
                    description: '',
                    version: version,

		    rootCollectionIdentity: config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + collectionId + '/' + collectionId + '_collection/' + version,
                    newRootCollectionDisplayId: collectionId + '_collection',
		    newRootCollectionVersion: version,
                    ownedByURI: config.get('databasePrefix') + 'user/' + req.user.username,
                    creatorName: '',
                    citationPubmedIDs: [],
		    overwrite_merge: overwrite_merge

                })

            }).then((result) => {

                const { success, log, errorLog, resultFilename } = result

                if(!success) {

                    const locals = {
                        config: config.get(),
                        section: 'invalid',
                        user: req.user,
                        errors: [ errorLog ]
                    }

                    res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))

                    return
                }
		
	        console.log('upload')

		return sparql.uploadFile(req.user.graphUri, resultFilename, 'application/rdf+xml').then(function redirectManage(next) {
		    return res.redirect('/manage');
		})
	    })		   
    }
};


