const {
  getCollectionMetaData
} = require('../query/collection')

var pug = require('pug')

const {
  fetchSBOLObjectRecursive
} = require('../fetch/fetch-sbol-object-recursive')

const serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var loadTemplate = require('../loadTemplate')

var extend = require('xtend')

var getUrisFromReq = require('../getUrisFromReq')

var sparql = require('../sparql/sparql')

const tmp = require('tmp-promise')

var fs = require('mz/fs')

const prepareSubmission = require('../prepare-submission')

module.exports = function (req, res) {
  req.setTimeout(0) // no timeout

  function addToCollectionsForm (req, res, collectionId, version, locals) {
    var collectionQuery = 'PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX sbol2: <http://sbols.org/v2#> SELECT ?object ?name WHERE { ?object a sbol2:Collection . FILTER NOT EXISTS { ?otherCollection sbol2:member ?object } OPTIONAL { ?object dcterms:title ?name . }}'

    function sortByNames (a, b) {
      if (a.name < b.name) {
        return -1
      } else {
        return 1
      }
    }

    return sparql.queryJson(collectionQuery, null).then((collections) => {
      collections.forEach((result) => {
        result.uri = result.object
        result.name = result.name ? result.name : result.uri.toString()
        delete result.object
      })
      collections.sort(sortByNames)

      locals = extend({
        config: config.get(),
        section: 'makePublic',
        user: req.user,
        collections: collections,
        submission: {
          id: collectionId || '',
          version: version || '',
          name: '',
          description: '',
          citations: ''
        },
        errors: {}
      }, locals)
      res.send(pug.renderFile('templates/views/makePublic.jade', locals))
    })
  }

  var overwriteMerge = '0'
  var collectionId = req.params.collectionId
  var version = req.params.version
  var name = ''
  var description = ''
  var citations = []
  var collectionUri

  const { graphUri, uri } = getUrisFromReq(req, res)

  if (req.method === 'POST') {
    overwriteMerge = req.body.tabState === 'new' ? '0' : '2'
    collectionId = req.body.id
    version = req.body.version
    collectionUri = req.body.collections
    name = req.body.name
    description = req.body.description
    citations = req.body.citations
    if (citations) {
      citations = citations.split(',').map(function (pubmedID) {
        return pubmedID.trim()
      }).filter(function (pubmedID) {
        return pubmedID !== ''
      })
    } else {
      citations = []
    }

    var errors = []
    if (overwriteMerge === '0') {
      if (collectionId === '') {
        errors.push('Please enter an id for your submission')
      }

      if (version === '') {
        errors.push('Please enter a version for your submission')
      }

      collectionUri = config.get('databasePrefix') + 'public/' + collectionId + '/' + collectionId + '_collection/' + version
    } else {
      if (!collectionUri || collectionUri === '') {
        errors.push('Please select a collection to add to')
      }

      var tempStr = collectionUri.replace(config.get('databasePrefix') + 'public/', '')
      collectionId = tempStr.substring(0, tempStr.indexOf('/'))
      version = tempStr.replace(collectionId + '/' + collectionId + '_collection/', '')
    }

    if (errors.length > 0) {
      if (!req.accepts('text/html')) {
        res.status(400).type('text/plain').send(errors)
        return
      } else {
        return addToCollectionsForm(req, res, collectionId, version, {
          errors: errors
        })
      }
    }
  } else {
    return addToCollectionsForm(req, res, collectionId, version, {})
  }

  console.log('getting collection')

  var sbol

  console.log('uri:' + uri)
  console.log('graphUri:' + graphUri)

  fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
    sbol = result.sbol

    if (version === 'current') version = '1'

    var uri = collectionUri

    console.log('check if exists:' + uri)

    return getCollectionMetaData(collectionUri, null /* public store */).then((result) => {
      if (!result) {
        /* not found */
        console.log('not found')
        if (overwriteMerge === '0') {
          return makePublic()
        } else {
          if (!req.accepts('text/html')) {
            res.status(400).type('text/plain').send('Submission id ' + collectionId + ' version ' + version + ' not found')
            return
          } else {
            return addToCollectionsForm(req, res, collectionId, version, {
              errors: ['Submission id ' + collectionId + ' version ' + version + ' not found']
            })
          }
        }
      }

      const metaData = result

      if (overwriteMerge === '0') {
        // Prevent make public
        console.log('prevent')
        if (!req.accepts('text/html')) {
          res.status(400).type('text/plain').send('Submission id ' + collectionId + ' version ' + version + ' already in use')
        } else {
          return addToCollectionsForm(req, res, collectionId, version, {
            errors: ['Submission id ' + collectionId + ' version ' + version + ' already in use']
          })
        }
      } else {
        // Merge
        console.log('merge')
        collectionId = metaData.displayId.replace('_collection', '')
        version = metaData.version

        return makePublic()
      }
    })
  }).catch((err) => {
    if (!req.accepts('text/html')) {
      res.status(500).type('text/plain').send(err.stack)
    } else {
      const locals = {
        config: config.get(),
        section: 'errors',
        user: req.user,
        errors: [err.stack]
      }
      res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    }
  })

  function saveTempFile () {
    return tmp.tmpName().then((tmpFilename) => {
      return fs.writeFile(tmpFilename, serializeSBOL(sbol)).then(() => {
        return Promise.resolve(tmpFilename)
      })
    })
  }

  function makePublic () {
    console.log('-- validating/converting')

    return saveTempFile().then((tmpFilename) => {
      console.log('tmpFilename is ' + tmpFilename)

      return prepareSubmission(tmpFilename, {

        uriPrefix: config.get('databasePrefix') + 'public/' + collectionId + '/',

        name: name || '',
        description: description || '',
        version: version,

        keywords: [],

        rootCollectionIdentity: config.get('databasePrefix') + 'public/' + collectionId + '/' + collectionId + '_collection' + '/' + version,
        newRootCollectionDisplayId: collectionId + '_collection',
        newRootCollectionVersion: version,
        ownedByURI: config.get('databasePrefix') + 'user/' + req.user.username,
        creatorName: '',
        citationPubmedIDs: citations,
        overwrite_merge: overwriteMerge

      })
    }).then((result) => {
      const { success, errorLog, resultFilename } = result

      if (!success) {
        if (!req.accepts('text/html')) {
          res.status(400).type('text/plain').send(errorLog)
        } else {
          const locals = {
            config: config.get(),
            section: 'invalid',
            user: req.user,
            errors: [errorLog]
          }
          res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
        }
        return
      }

      console.log('upload')

      return sparql.uploadFile(null, resultFilename, 'application/rdf+xml').then(function removeSubmission (next) {
        if (req.params.version !== 'current') {
          console.log('remove')

          var designId = req.params.collectionId + '/' + req.params.displayId + '/' + version
          var uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId

          var uriPrefix = uri.substring(0, uri.lastIndexOf('/'))
          if (uriPrefix.endsWith('_collection')) {
            uriPrefix = uriPrefix.substring(0, uriPrefix.lastIndexOf('/') + 1)
          }

          var templateParams = {
            collection: uri,
            uriPrefix: uriPrefix,
            version: version
          }

          var removeQuery = loadTemplate('sparql/removeCollection.sparql', templateParams)
          console.log(removeQuery)

          return sparql.deleteStaggered(removeQuery, graphUri).then(() => {
            templateParams = {
              uri: uri
            }
            removeQuery = loadTemplate('sparql/remove.sparql', templateParams)
            sparql.deleteStaggered(removeQuery, graphUri).then(() => {
              console.log('update collection membership')
              var d = new Date()
              var modified = d.toISOString()
              modified = modified.substring(0, modified.indexOf('.'))
              const updateQuery = loadTemplate('./sparql/UpdateCollectionMembership.sparql', {
                modified: JSON.stringify(modified)
              })
              sparql.updateQuery(updateQuery, null).then((result) => {
                if (!req.accepts('text/html')) {
                  res.status(200).type('text/plain').send('Success')
                } else {
                  res.redirect('/manage')
                }
              })
            })
          })
        } else {
          if (!req.accepts('text/html')) {
            return res.status(200).type('text/plain').send('Success')
          } else {
            return res.redirect('/manage')
          }
        }
      })
    })
  }
}
