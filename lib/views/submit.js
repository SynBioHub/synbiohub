const { getCollectionMetaData } = require('../query/collection')
const loadTemplate = require('../loadTemplate')
const pug = require('pug')
const fs = require('mz/fs')
const extend = require('xtend')
const config = require('../config')
const sparql = require('../sparql/sparql')
const prepareSubmission = require('../prepare-submission')
const multiparty = require('multiparty')
const tmp = require('tmp-promise')
const apiTokens = require('../apiTokens')
const attachments = require('../attachments')
const uploads = require('../uploads')
const exec = require('child_process').exec
const plugins = require('../plugins')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    submitPost(req, res)
  } else {
    submitForm(req, res, {}, {})
  }
}

async function submitPlugin (pluginIdx, filename) {
  if (pluginIdx === 'default') {
    return filename
  }

  let plugin = config.get('plugins')['submit'][pluginIdx]
  console.log(JSON.stringify(plugin))
  console.log(`Plugin index: ${pluginIdx}`)

  try {
    let pluginUp = await plugins.testSubmitPlugin(plugin, filename)
    if (!pluginUp) {
      // Should just submit the original file
      return filename
    }

    let resultFilename = await plugins.runSubmitPlugin(plugin, filename)
    console.log(`Plugin returned ${resultFilename}`)
    return resultFilename
  } catch (err) {
    console.error('There was an error with a submit plugin.')
    console.log('Falling back to default submit')
    return filename
  }
}

function submitForm (req, res, submissionData, locals) {
  var collectionQuery = 'PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX sbol2: <http://sbols.org/v2#> SELECT ?object ?name WHERE { ?object a sbol2:Collection . OPTIONAL { ?object dcterms:title ?name . } }'
  var subCollections

  var rootCollectionQuery = 'PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX sbol2: <http://sbols.org/v2#> SELECT ?object ?name WHERE { ?object a sbol2:Collection . FILTER NOT EXISTS { ?otherCollection sbol2:member ?object } OPTIONAL { ?object dcterms:title ?name . }}'
  var rootCollections

  function sortByNames (a, b) {
    if (a.name < b.name) {
      return -1
    } else {
      return 1
    }
  }

  return sparql.queryJson(rootCollectionQuery, req.user.graphUri).then((collections) => {
    collections.forEach((result) => {
      result.uri = result.object
      result.name = result.name ? result.name : result.uri.toString()
      delete result.object
    })
    collections.sort(sortByNames)
    rootCollections = collections

    sparql.queryJson(collectionQuery, null).then(async (collections2) => {
      collections2.forEach((result) => {
        result.uri = result.object
        result.name = result.name ? result.name : result.uri.toString()
        delete result.object
      })
      collections2.sort(sortByNames)
      subCollections = collections2

      let submitPlugins = await plugins.getSubmitPlugins()

      submissionData = extend({
        id: '',
        version: '1',
        name: '',
        description: '',
        citations: '', // comma separated pubmed IDs
        collectionChoices: [],
        overwriteMerge: '0',
        // createdBy: req.url==='/remoteSubmit'?JSON.parse(req.body.user):req.user,
        createdBy: req.user,
        file: '',
        plugin: 'default'
      }, submissionData)

      locals = extend({
        config: config.get(),
        section: 'submit',
        user: req.user,
        submission: submissionData,
        collections: subCollections,
        rootCollections: rootCollections,
        plugins: submitPlugins,
        errors: []
      }, locals)

      res.send(pug.renderFile('templates/views/submit.jade', locals))
    })
  })
}

function submitPost (req, res) {
  req.setTimeout(0) // no timeout

  const form = new multiparty.Form()

  form.on('error', (err) => {
    console.error(err)
    res.status(500).send(err)
  })

  var overwriteMerge = 'unset'
  var collectionUri
  var collectionId = ''
  var version = ''
  var name = ''
  var description = ''
  var citations = ''
  var plugin = 'default'

  form.parse(req, (err, fields, files) => {
    function getUser () {
      if (req.user) {
        return Promise.resolve(req.user)
      } else {
        // TODO: Note this should be redundant now that user info is always coming in via header
        console.log('user:' + fields.user[0])

        var token = apiTokens.getUserFromToken(fields.user[0])
        if (token) {
          return token
        } else {
          return Promise.reject(new Error('Invalid user token'))
        }
      }
    }

    getUser().then((user) => {
      if (err) {
        if (req.forceNoHTML || !req.accepts('text/html')) {
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
      }

      // TODO: this code is major hack.  Needs to be cleaned up.
      var collectionChoices = fields.collectionChoices
      if (req.forceNoHTML || !req.accepts('text/html')) {
        if (fields.collectionChoices && fields.collectionChoices[0]) {
          collectionChoices = fields.collectionChoices[0].split(',')
        }
      }

      if (fields.overwrite_merge && fields.overwrite_merge[0]) {
        overwriteMerge = fields.overwrite_merge[0]
      } else {
        if (fields.submitType[0] === 'new') {
          overwriteMerge = 0
        } else {
          overwriteMerge = 2
        }

        if (fields.overwrite_objects && fields.overwrite_objects[0]) {
          overwriteMerge = overwriteMerge + 1
        }
        overwriteMerge = overwriteMerge.toString()
      }

      if (fields.id && fields.id[0]) {
        collectionId = fields.id[0].trim()
      }
      if (fields.version && fields.version[0]) {
        version = fields.version[0].trim()
      }
      if (fields.name && fields.name[0]) {
        name = fields.name[0].trim()
      }
      if (fields.description && fields.description[0]) {
        description = fields.description[0].trim()
      }
      if (fields.citations && fields.citations[0]) {
        citations = fields.citations[0].trim()
      }
      if (fields.rootCollections && fields.rootCollections[0]) {
        collectionUri = fields.rootCollections[0]
      }

      if (fields.plugin && fields.plugin[0]) {
        plugin = fields.plugin[0].trim()
      }

      const submissionData = {
        id: collectionId,
        version: version,
        name: name,
        description: description,
        citations: citations,
        collectionChoices: collectionChoices || [],
        overwriteMerge: overwriteMerge,
        createdBy: req.user ? req.user : user,
        plugin: plugin
      }

      var errors = []

      if (submissionData.createdBy === undefined) {
        errors.push('Must be logged in to submit')
      }

      if (overwriteMerge === '0' || overwriteMerge === '1') {
        if (submissionData.id === '') {
          errors.push('Please enter an id for your submission')
        }

        const idRegEx = new RegExp('^[a-zA-Z_]+[a-zA-Z0-9_]*$')
        if (!idRegEx.test(submissionData.id)) {
          errors.push('Collection id is invalid. An id is a string of characters that MUST be composed of only alphanumeric or underscore characters and MUST NOT begin with a digit.')
        }

        if (submissionData.version === '') {
          errors.push('Please enter a version for your submission')
        }

        const versionRegEx = /^[0-9]+[a-zA-Z0-9_\\.-]*$/
        var tempStr
        if (!versionRegEx.test(submissionData.version)) {
          errors.push('Version is invalid. A version is a string of characters that MUST be composed of only alphanumeric characters, underscores, hyphens, or periods and MUST begin with a digit.')
        }

        if (submissionData.name === '') {
          errors.push('Please enter a name for your submission')
        }

        if (submissionData.description === '') {
          errors.push('Please enter a brief description for your submission')
        }

        collectionUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version
      } else {
        if (!collectionUri || collectionUri === '') {
          if (!submissionData.createdBy.username || !submissionData.id || !submissionData.version) {
            errors.push('Please select a collection to add to')
          } else {
            collectionUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version
            tempStr = collectionUri.replace(config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/', '')
            collectionId = submissionData.id + '_collection'
            version = submissionData.version
            console.log('collectionId:' + collectionId)
            console.log('version:' + version)
          }
        } else {
          tempStr = collectionUri.replace(config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/', '')
          collectionId = tempStr.substring(0, tempStr.indexOf('/'))
          version = tempStr.replace(collectionId + '/' + collectionId + '_collection/', '')
          console.log('collectionId:' + collectionId)
          console.log('version:' + version)
        }
      }

      const citationRegEx = /^[0-9]+(,[0-9]*)*$/
      if (submissionData.citation && submissionData.citation.trim() !== '' &&
!citationRegEx.test(submissionData.citations)) {
        errors.push('Citations must be comma separated Pubmed IDs')
      }

      if (errors.length > 0) {
        if (req.forceNoHTML || !req.accepts('text/html')) {
          res.status(400).type('text/plain').send(errors)
          return
        } else {
          return submitForm(req, res, submissionData, {
            errors: errors
          })
        }
      }

      if (submissionData.citations) {
        submissionData.citations = submissionData.citations.split(',').map(function (pubmedID) {
          return pubmedID.trim()
        }).filter(function (pubmedID) {
          return pubmedID !== ''
        })
      } else {
        submissionData.citations = []
      }

      var graphUri
      var uri

      graphUri = submissionData.createdBy.graphUri

      uri = collectionUri

      getCollectionMetaData(uri, graphUri).then((result) => {
        if (!result) {
          console.log('not found')
          if (submissionData.overwriteMerge === '2' || submissionData.overwriteMerge === '3') {
            if (req.forceNoHTML || !req.accepts('text/html')) {
              res.status(500).type('text/plain').send('Submission id and version does not exist')
              return
            } else {
              errors.push('Submission id and version does not exist')
              submitForm(req, res, submissionData, {
                errors: errors
              })
            }
          }
          submissionData.overwriteMerge = '0'
          return doSubmission()
        }

        const metaData = result

        if (submissionData.overwriteMerge === '2' || submissionData.overwriteMerge === '3') {
          // Merge
          console.log('merge')
          collectionId = metaData.displayId.replace('_collection', '')
          version = metaData.version
          submissionData.name = metaData.name || ''
          submissionData.description = metaData.description || ''

          return doSubmission()
        } else if (submissionData.overwriteMerge === '1') {
          // Overwrite
          console.log('overwrite')
          return doSubmission()
        } else {
          // Prevent make public
          console.log('prevent')

          if (req.forceNoHTML || !req.accepts('text/html')) {
            console.log('prevent')
            res.status(500).type('text/plain').send('Submission id and version already in use')
          } else {
            errors.push('Submission id and version already in use')

            submitForm(req, res, submissionData, {
              errors: errors
            })
          }
        }
      })/* .catch((err) => {
        if (req.forceNoHTML || !req.accepts('text/html')) {
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
      }) */

      function saveTempFile () {
        if (files.file) {
          return Promise.resolve(files.file[0].path)
        } else {
          return tmp.tmpName().then((tmpFilename) => {
            return fs.writeFile(tmpFilename, fields.file[0]).then(() => {
              return Promise.resolve(tmpFilename)
            })
          })
        }
      }

      function doSubmission () {
        console.log('-- validating/converting')

        return saveTempFile().then(async (tmpFilename) => {
          console.log('Running submit plugin')
          let pluginResult = await submitPlugin(submissionData.plugin, tmpFilename)

          tmpFilename = pluginResult

          return prepareSubmission(tmpFilename, {
            submit: true,
            uriPrefix: config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + collectionId + '/',

            name: name,
            description: description,
            version: version,

            rootCollectionIdentity: config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + collectionId + '/' + collectionId + '_collection/' + version,
            newRootCollectionDisplayId: collectionId + '_collection',
            newRootCollectionVersion: version,
            ownedByURI: config.get('databasePrefix') + 'user/' + submissionData.createdBy.username,
            creatorName: submissionData.createdBy.name,
            citationPubmedIDs: submissionData.citations,
            collectionChoices: submissionData.collectionChoices,
            overwrite_merge: overwriteMerge
          })
        }).then((result) => {
          const { success, log, errorLog, resultFilename, attachmentFiles, extractDirPath } = result

          console.log(log)
          console.log(JSON.stringify(attachmentFiles))

          if (!success) {
            if (req.forceNoHTML || !req.accepts('text/html')) {
              res.status(500).type('text/plain').send(errorLog)
              return
            } else {
              const locals = {
                config: config.get(),
                section: 'invalid',
                user: req.user,
                errors: [errorLog]
              }

              res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
              return
            }
          }
          if (submissionData.overwriteMerge === '1') {
            var uriPrefix = uri.substring(0, uri.lastIndexOf('/'))
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
              return sparql.deleteStaggered(removeQuery, graphUri).then(() => {
                return { success, log, errorLog, resultFilename, attachmentFiles, extractDirPath }
              })
            })
          }
          return { success, log, errorLog, resultFilename, attachmentFiles, extractDirPath }
        }).then(({ success, log, errorLog, resultFilename, attachmentFiles, extractDirPath }) => {
          return sparql.uploadFile(submissionData.createdBy.graphUri, resultFilename, 'application/rdf+xml').then((result) => {
            let baseURI = config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + collectionId
            let collectionURI = baseURI + '/' + collectionId + '_collection/' + version

            let sourceQuery = loadTemplate('sparql/GetAttachmentSourceFromTopLevel.sparql', { uri: collectionURI })

            sparql.queryJson(sourceQuery, submissionData.createdBy.graphUri).then(results => {
              let sources = {}

              results.forEach(result => {
                var filename = result['source']
                uri = result['attachment']

                sources[filename] = uri
              })

              return Promise.all(Object.keys(attachmentFiles).map(filename => {
                if (attachmentFiles[filename] && attachmentFiles[filename].toLowerCase().indexOf('sbol') >= 0) { return Promise.resolve() }

                let fileStream = fs.createReadStream(filename)

                return uploads.createUpload(fileStream).then((uploadInfo) => {
                  const { hash, size, mime } = uploadInfo
                  let originalFilename = filename

                  if (filename.indexOf('/') >= 0) {
                    filename = filename.substr(filename.lastIndexOf('/') + 1)
                  }

                  var key = 'file:' + filename

                  if (sources[key]) {
                    return attachments.updateAttachment(
                      submissionData.createdBy.graphUri,
                      sources[key],
                      hash,
                      size)
                  }

                  return attachments.addAttachmentToTopLevel(
                    submissionData.createdBy.graphUri,
                    baseURI,
                    collectionURI,
                    filename,
                    hash,
                    size,
                    attachmentFiles[originalFilename] || mime,
                    submissionData.createdBy.username)
                }).then(attachmentUri => {
                  var badFileUri = 'file:' + filename
                  var goodFileUri = attachmentUri

                  let query = loadTemplate('./sparql/AttachmentUpdate.sparql', { oldUri: badFileUri, newUri: goodFileUri })
                  return sparql.updateQuery(query, submissionData.createdBy.graphUri)
                })
              })).then(() => {
                console.log('rm -r ' + extractDirPath)
                exec('rm -r ' + extractDirPath)
              })
            })
          }).then(() => {
            console.log('unlinking:' + resultFilename)
            fs.unlink(resultFilename)

            console.log('unlinking:' + resultFilename)
            fs.unlink(resultFilename)

            if (req.forceNoHTML || !req.accepts('text/html')) {
              res.status(200).type('text/plain').send('Successfully uploaded')
            } else {
              res.redirect('/user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + collectionId + '/' + collectionId + '_collection/' + version)
            }
          })
        })
      }
    }).catch((err) => {
      if (req.forceNoHTML || !req.accepts('text/html')) {
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
  })
}
