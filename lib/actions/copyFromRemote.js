
const { getCollectionMetaData } = require('../query/collection')

var pug = require('pug')

var async = require('async');

var request = require('request')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var config = require('../config')

var loadTemplate = require('../loadTemplate')

var extend = require('xtend')

var getUrisFromReq = require('../getUrisFromReq')

var sparql = require('../sparql/sparql')

const tmp = require('tmp-promise')

var fs = require('mz/fs');

const prepareSubmission = require('../prepare-submission')

const serializeSBOL = require('../serializeSBOL')

module.exports = function(req, res) {

    var overwrite_merge = '0'
    var collectionId = req.params.collectionId
    var version = req.params.version

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
		section: 'copyFromRemote',
		user: req.user,
                submission: { id: collectionId || '',
			      version: version || ''
			    },
		errors: errors
	    }, locals)
	    res.send(pug.renderFile('templates/views/clone.jade', locals))
	    return
	}
  
    } 

    console.log('getting collection')

    var sbol
    var collection

    console.log('uri:'+uri)
    console.log('graphUri:'+req.user.graphUri)

    fetchSBOLObjectRecursive('Collection', uri, req.user.graphUri).then((result) => {

        sbol = result.sbol
        collection = result.object

	if (version==='current') version = '1'

	console.log('collection:'+collection)

	var graphUri = req.user.graphUri

        var uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + collectionId + '/' + collectionId + '_collection' + '/' + version

	console.log('check if exists already')

        return getCollectionMetaData(uri, graphUri).then((result) => {

            if(!result) {

                /* not found */
		overwrite_merge = '0'
                return copyFromRemote()

            }

            const metaData = result
        
            if (overwrite_merge === '0') {
                // Prevent make public
                console.log('prevent')
                var locals = {}
                locals = extend({
                    config: config.get(),
                    section: 'copyFromRemote',
                    user: req.user,
                    submission: { id: collectionId || '',
                        version: version || ''
                    },
                    errors: [ 'Submission id ' + collectionId + ' version ' + version + ' already in use' ]
                }, locals)
                res.send(pug.renderFile('templates/views/clone.jade', locals))

            } else {
                // Merge
                console.log('merge')
                collection.name = metaData.name || ''
                collection.description = metaData.description || ''

                return copyFromRemote()

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

    function copyFromRemote() {

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


