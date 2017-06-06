
const { getCollectionMetaData } = require('../query/collection')

var pug = require('pug')

var async = require('async');

var request = require('request')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

const serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var loadTemplate = require('../loadTemplate')

var extend = require('xtend')

var getUrisFromReq = require('../getUrisFromReq')

var sparql = require('../sparql/sparql')

const tmp = require('tmp-promise')

var fs = require('mz/fs');

const prepareSubmission = require('../prepare-submission')

module.exports = function (req, res) {

    req.setTimeout(0) // no timeout

    function copyFromRemoteForm(req, res, collectionId, version, locals) {

        var collectionQuery = 'PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX sbol2: <http://sbols.org/v2#> SELECT ?object ?name WHERE { ?object a sbol2:Collection . FILTER NOT EXISTS { ?otherCollection sbol2:member ?object } OPTIONAL { ?object dcterms:title ?name . }}'
        var collections

        function sortByNames(a, b) {
            if (a.name < b.name) {
                return -1
            } else {
                return 1
            }
        }

        return sparql.queryJson(collectionQuery, req.user.graphUri).then((collections) => {

            collections.forEach((result) => {
                result.uri = result.object
                result.name = result.name ? result.name : result.uri.toString()
                delete result.object
            })
            collections.sort(sortByNames)

            locals = extend({
                config: config.get(),
                section: 'copyFromRemote',
                user: req.user,
                collections: collections,
                submission: {
                    id: collectionId || '',
                    version: '1',
                    name: '',
                    description: '',
                    citations: ''
                },
                errors: {}
            }, locals)
            res.send(pug.renderFile('templates/views/copyFromRemote.jade', locals))
            return
        })
    }

    var overwrite_merge = "unset"
    var collectionId = req.params.collectionId
    var version = req.params.version
    var name
    var description
    var citations = []

    const { graphUri, uri, designId } = getUrisFromReq(req)

    if (req.method === 'POST') {

        if(req.body.copyType === "new") {
            overwrite_merge = 0
        } else {
            overwrite_merge = 2
        }

        if(req.body.overwrite_objects) {
            overwrite_merge = overwrite_merge + 1;
        }

        overwrite_merge = overwrite_merge.toString()
        console.log(overwrite_merge)
        collectionId = req.body.id
        version = req.body.version
        collectionUri = req.body.collections
        name = req.body.name
        description = req.body.description
        citations = req.body.citations
        if (citations) {
            citations = citations.split(',').map(function (pubmedID) {
                return pubmedID.trim();
            }).filter(function (pubmedID) {
                return pubmedID !== '';
            });
        } else {
            citations = []
        }

        var errors = []
        if (overwrite_merge === "0" || overwrite_merge === "1") {

            if (collectionId === '') {
                errors.push('Please enter an id for your submission')
            }

            if (version === '') {
                errors.push('Please enter a version for your submission')
            }

            collectionUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + collectionId + '/' + collectionId + '_collection' + '/' + version

        } else {

            if (!collectionUri || collectionUri === '') {
                errors.push('Please select a collection to add to')
            }

            var tempStr = collectionUri.replace(config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/', '')
            collectionId = tempStr.substring(0, tempStr.indexOf('/'))
            version = tempStr.replace(collectionId + '/' + collectionId + '_collection/', '')
            console.log('collectionId:' + collectionId)
            console.log('version:' + version)

        }

        if (errors.length > 0) {
            return copyFromRemoteForm(req, res, collectionId, version, {
                errors: errors
            })
        }

    } else {
        return copyFromRemoteForm(req, res, collectionId, version, {})
    }

    console.log('getting collection')

    var sbol
    var collection
    var newRootCollectionDisplayId = collectionId + '_collection'

    console.log('uri:' + uri)
    console.log('graphUri:' + req.user.graphUri)

    fetchSBOLObjectRecursive(uri, req.user.graphUri).then((result) => {

        sbol = result.sbol
        collection = result.object

        if (version === 'current') version = '1'

        var graphUri = req.user.graphUri

        var uri = collectionUri

        console.log('check if exists:' + uri)

        return getCollectionMetaData(uri, graphUri).then((result) => {

            if (!result) {

                /* not found */
                console.log('not found')
                if (overwrite_merge === '0') {
                    return copyFromRemote()
                } else {
                    // NOTE: this should never happen, since chosen from a list of existing collections
                    return copyFromRemoteForm(req, res, collectionId, version, {
                        errors: ['Submission id ' + collectionId + ' version ' + version + ' not found']
                    })
                }

            }

            const metaData = result

            if (overwrite_merge === '0') {
                // Prevent make public
                console.log('prevent')
                return copyFromRemoteForm(req, res, collectionId, version, {
                    errors: ['Submission id ' + collectionId + ' version ' + version + ' already in use']
                })

            } else if (overwrite_merge === '1') {
                // Overwrite
                console.log('overwrite')
                uriPrefix = uri.substring(0, uri.lastIndexOf('/'))
                uriPrefix = uriPrefix.substring(0, uriPrefix.lastIndexOf('/') + 1)

                var templateParams = {
                    uriPrefix: uriPrefix,
                    version: version
                }

                var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

                return sparql.deleteStaggered(removeQuery, req.user.graphUri).then(copyFromRemote())

            } else {
                // Merge
                console.log('merge')
                collectionId = metaData.displayId.replace('_collection', '')
                version = metaData.version

                return copyFromRemote()

            }
        })

    }).catch((err) => {

        const locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [err.stack]
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

                name: name || '',
                description: description || '',
                version: version,

                rootCollectionIdentity: config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + collectionId + '/' + collectionId + '_collection/' + version,
                newRootCollectionDisplayId: collectionId + '_collection',
                newRootCollectionVersion: version,
                ownedByURI: config.get('databasePrefix') + 'user/' + req.user.username,
                creatorName: req.user.name,
                citationPubmedIDs: citations,
                overwrite_merge: overwrite_merge

            })

        }).then((result) => {

            const { success, log, errorLog, resultFilename } = result

            if (!success) {

                const locals = {
                    config: config.get(),
                    section: 'invalid',
                    user: req.user,
                    errors: [errorLog]
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


