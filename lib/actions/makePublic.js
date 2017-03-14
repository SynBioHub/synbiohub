
const { getCollectionMetaData } = require('../query/collection')

var pug = require('pug')

var async = require('async');

var request = require('request')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

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

    var sbol
    var collection

    console.log('uri:'+uri)
    console.log('graphUri:'+req.user.graphUri)

    getCollection(uri, [ req.user.graphUri ]).then((result) => {

        sbol = result.sbol
        collection = result.object

	console.log('collection:'+collection)

        var uri = config.get('databasePrefix') + 'public/' + collection.displayId.replace('_collection','') + '/' + collection.displayId + '/' + collection.version

	console.log('check if exists already')

        return getCollectionMetaData(uri, null /* public store */).then((result) => {

            if(!result) {

                /* not found */
		overwrite_merge = '0'
                return makePublic()

            }

            const metaData = result.metaData
        
            if (overwrite_merge === '0') {
                // Prevent make public
                console.log('prevent')
                var locals = {}
                locals = extend({
                    config: config.get(),
                    section: 'makePublic',
                    user: req.user,
                    submission: { id: req.params.collectionId || '',
                        version: req.params.version || ''
                    },
                    errors: [ 'Submission id ' + collection.displayId.replace('_collection','') + ' version ' + collection.version + ' already in use' ]
                }, locals)
                res.send(pug.renderFile('templates/views/makePublic.jade', locals))

            } else if (overwrite_merge === '1') {
                // Overwrite
                console.log('overwrite')
                uriPrefix = uri.substring(0,uri.lastIndexOf('/'))
                uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/')+1)

                var templateParams = {
                    uriPrefix: uriPrefix,
                    version: req.params.version
                }

                var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

                return sparql.deleteStaggered(removeQuery, null).then(makePublic())

            } else {
                // Merge
                console.log('merge')
                collection.name = metaData.name || ''
                collection.description = metaData.description || ''

                return makePublic()

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

    function makePublic() {

            console.log('-- validating/converting');

            return saveTempFile().then((tmpFilename) => {

                console.log('tmpFilename is ' + tmpFilename)
                
                return prepareSubmission(tmpFilename, {

                    uriPrefix: config.get('databasePrefix') + 'public/' + collectionId + '/',

                    name: '',
                    description: '',
                    version: version,

                    keywords: [],

		    rootCollectionIdentity: config.get('databasePrefix') + 'public/' + collectionId + '/' + collectionId + '_collection/' + version,
                    newRootCollectionDisplayId: '',
		    newRootCollectionVersion: version,
                    ownedByURI: '',
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

		return sparql.uploadFile(null, resultFilename, 'application/rdf+xml').then(function removeSubmission(next) {

		    console.log('remove')

		    var designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		    var uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
		    
		    var uriPrefix = uri.substring(0,uri.lastIndexOf('/'))
		    uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/')+1)

		    var templateParams = {
			uriPrefix: uriPrefix,
			version: req.params.version
		    }

		    var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)
		    console.log(removeQuery)
		    return sparql.deleteStaggered(removeQuery, req.user.graphUri).then(() => {
			res.redirect('/manage');
		    })
		})
	    })		   
    }
};


