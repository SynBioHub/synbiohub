var getCollectionMetaData = require('../get-sbol').getCollectionMetaData

var pug = require('pug')

var async = require('async');

var request = require('request')

var getCollection = require('../get-sbol').getCollection

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var loadTemplate = require('../loadTemplate')

var extend = require('xtend')

var getUrisFromReq = require('../getUrisFromReq')

var sparql = require('../sparql/sparql')

module.exports = function(req, res) {

    var overwrite_merge = '0'
    var collectionId = req.params.collectionId
    var version = req.params.version

	const { graphUris, uri, designId } = getUrisFromReq(req)

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

    console.log('check if exists already')

    getCollection(uri, [ req.user.graphUri ]).then((result) => {

        sbol = result.sbol
        collection = result.object

        var graphUris = [
            null /* public store */
        ]

        var uri = config.get('databasePrefix') + 'public/' + collection.displayId.replace('_collection','') + '/' + collection.displayId + '/' + collection.version

        return getCollectionMetaData(uri, graphUris).then((result) => {

            if(!result) {

                /* not found */

                return makePublic()

            }

            const metaData = result.metaData
        
            if (overwrite_merge === '0') {
                // Prevent make public
                console.log('prevent')
                var locals = {}
                locals = extend({
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
                uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/'))

                var templateParams = {
                    uriPrefix: uriPrefix,
                    version: req.params.version
                }

                var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

                return sparql.queryJson(removeQuery, null).then(makePublic())

            } else {
                // Merge
                console.log('merge')
                collection.name = _metaData[0].name || ''
                collection.description = _metaData[0].description || ''

                return makePublic()

            }
        })

    }).catch((err) => {

        const locals = {
            section: 'errors',
            user: req.user,
            errors: [ err.stack ]
        }

        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    })

    function makePublic() {

        return new Promise((resolve, reject) => {

            console.log('convert')
            var xml = serializeSBOL(sbol)

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
                    'uri_prefix': config.get('databasePrefix') + 'public/' + collectionId + '/',
                    'version': version,
                    'insert_type': false,
                    'main_file_name': 'main file',
                    'diff_file_name': 'comparison file',
                },
                    'return_file': true,
                    'main_file': xml
                }
            }, function(err, response, body) {
                if(err || response.statusCode >= 300) {

                    reject(err || new Error('HTTP ' + response.statusCode))

                } else {

                    if (!body.valid) {

                        const locals = {
                            section: 'invalid',
                            user: req.user,
                            errors: body.errors
                        }

                        res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))

                    } else {

                        const convertedSBOL = body.result

                        resolve(convertedSBOL)
                    }
                }
            })

        }).then(function upload(xml) {

            console.log('upload')

            return sparql.upload(null, xml, 'application/rdf+xml')

        }).then(function removeSubmission(next) {

            console.log('remove')

            var designId
            var uri

            if(req.params.userId) {
                designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
                uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
            } else {
                designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
                uri = config.get('databasePrefix') + 'public/' + designId
            } 

            var uriPrefix = uri.substring(0,uri.lastIndexOf('/'))
            uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/'))

            var templateParams = {
                uriPrefix: uri,
                version: req.params.version
            }

            var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

            return sparql.queryJson(removeQuery, req.user.graphUri).then(() => {
                res.redirect('/manage');
            })
        })

    }

};


