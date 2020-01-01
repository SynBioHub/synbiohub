
const { getCollectionMetaData } = require('../query/collection')
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var loadTemplate = require('../loadTemplate')

var pug = require('pug')

var fs = require('mz/fs')

var extend = require('xtend')

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

const cloneSubmission = require('../clone-submission')

var sparql = require('../sparql/sparql')

const tmp = require('tmp-promise')

module.exports = function (req, res) {
  var submissionData
  if (req.method === 'POST') {
    submissionData = {
      id: req.body.id || '',
      version: req.body.version || '',
      overwriteMerge: req.body.overwrite_merge || ''
    }

    clonePost(req, res, submissionData)
  } else {
    submissionData = {
      id: req.params.collectionId || '',
      version: req.params.version || ''
    }

    cloneForm(req, res, submissionData, {})
  }
}

function cloneForm (req, res, submissionData, locals) {
  req.setTimeout(0) // no timeout

  locals = extend({
    config: config.get(),
    section: 'submit',
    user: req.user,
    submission: submissionData,
    errors: []
  }, locals)

  res.send(pug.renderFile('templates/views/clone.jade', locals))
}

function clonePost (req, res, submissionData) {
  var errors = []

  var overwriteMerge = req.body.overwrite_merge

  if (req.body.overwrite_objects && req.body.overwrite_objects[0]) {
    overwriteMerge = '1'
  }

  submissionData.id = submissionData.id.trim()
  submissionData.version = submissionData.version.trim()

  if (submissionData.id === '') {
    errors.push('Please enter an id for your submission')
  }

  if (submissionData.version === '') {
    errors.push('Please enter a version for your submission')
  }

  if (submissionData.id + '_collection' === req.params.displayId &&
submissionData.version === req.params.version) {
    errors.push('Please enter a different id or version for your submission')
  }

  if (errors.length > 0) {
    if (!req.accepts('text/html')) {
      res.status(400).type('text/plain').send(errors)
      return
    } else {
      return cloneForm(req, res, submissionData, {
        errors: errors
      })
    }
  }

  var sbol

  const { uri, graphUri } = getUrisFromReq(req, res)

  fetchSBOLObjectRecursive(uri, req.user.graphUri).then((result) => {
    sbol = result.sbol
  }).then(function retrieveCollectionMetaData (next) {
    var newUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version

    return getCollectionMetaData(newUri, graphUri).then((result) => {
      if (!result) {
        return doClone()
      }

      const metaData = result
      var uriPrefix

      if (overwriteMerge === '2' || overwriteMerge === '3') {
        // Merge
        console.log('merge')
        submissionData.name = metaData.name || ''
        submissionData.description = metaData.description || ''

        return doClone()
      } else if (overwriteMerge === '1') {
        // Overwrite
        console.log('overwrite')
        uriPrefix = uri.substring(0, uri.lastIndexOf('/'))
        uriPrefix = uriPrefix.substring(0, uriPrefix.lastIndexOf('/') + 1)

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
          res.status(400).type('text/plain').send('Submission id and version already in use')
        } else {
          errors.push('Submission id and version already in use')

          cloneForm(req, res, submissionData, {
            errors: errors
          })
        }
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
        errors: [ err.stack ]
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

  function doClone () {
    console.log('-- validating/converting')

    return saveTempFile().then((tmpFilename) => {
      console.log('tmpFilename is ' + tmpFilename)

      return cloneSubmission(tmpFilename, {
        uriPrefix: config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + submissionData.id + '/',

        version: submissionData.version,

        rootCollectionIdentity: config.get('databasePrefix') + 'user/' + encodeURIComponent(req.user.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection/' + submissionData.version,
        originalCollectionDisplayId: req.params.displayId,
        originalCollectionVersion: req.params.version,
        newRootCollectionDisplayId: submissionData.id + '_collection',
        newRootCollectionVersion: submissionData.version,
        overwriteMerge: submissionData.overwriteMerge

      })
    }).then((result) => {
      const { success, errorLog, resultFilename } = result

      if (!success) {
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

      console.log('uploading sbol...')

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
