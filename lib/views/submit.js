const { getCollectionMetaData } = require('../query/collection')
const loadTemplate = require('../loadTemplate')
const pug = require('pug')
const fs = require('mz/fs')
const extend = require('xtend')
const config = require('../config')
const sparql = require('../sparql/sparql')
const prepareSubmission = require('../prepare-submission')
const multiparty = require('multiparty')
const attachments = require('../attachments')
const uploads = require('../uploads')
const exec = require('child_process').exec
const plugins = require('../plugins')
const tmp = require('tmp-promise')

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

  let submissionData = {
    id: '',
    version: '',
    name: '',
    description: '',
    citations: '',
    collectionChoices: [],
    overwriteMerge: 'unset',
    createdBy: req.user,
    plugin: 'default'
  }

  // Abort on errors
  form.on('error', (err) => {
    console.error(err)
    res.status(500).send(err)
  })

  // Save file
  form.on('file', (name, file) => {
    submissionData.filename = file.path
  })

  // Parse all fields
  form.on('field', (name, value) => {
    switch (name) {
      case 'submitType':
        let typeNum = 0
        if (value !== 'new') {
          typeNum = 2
        }

        if (submissionData.overwriteMerge === 'unset') {
          submissionData.overwriteMerge = typeNum
        } else {
          submissionData.overwriteMerge += typeNum
        }
        break
      case 'overwrite_objects':
        if (submissionData.overwriteMerge === 'unset') {
          submissionData.overwriteMerge = 1
        } else {
          submissionData.overwriteMerge += 1
        }
        break
      case 'overwrite_merge':
        submissionData.overwriteMerge = value
        break
      case 'rootCollections':
        submissionData.collectionUri = value
        break
      case 'collectionChoices':
        if (req.forceNoHTML || !req.accepts('text/html')) {
          if (value) {
            submissionData.collectionChoices = value.split(',')
          }
        } else {
          submissionData.collectionChoices = value
        }
        break

      default:
      // In the default case the field name in the form matches the one in submissionData
        submissionData[name] = value
        break
    }
  })

  // When all fields are parsed, do the submission
  form.on('close', () => {
    handleSubmission(req, res, submissionData)
  })

  form.parse(req)
}

async function sanitizeSubmission (submissionData) {
  var errors = []

  if (submissionData.createdBy === undefined) {
    errors.push('Must be logged in to submit')
  }

  submissionData.overwriteMerge = submissionData.overwriteMerge.toString()

  if (submissionData.overwriteMerge === '0' || submissionData.overwriteMerge === '1') {
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

    submissionData.collectionUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version
    submissionData.collectionId = submissionData.id + '_collection'
  } else {
    if (!submissionData.collectionUri || submissionData.collectionUri === '') {
      if (!submissionData.createdBy.username || !submissionData.id || !submissionData.version) {
        errors.push('Please select a collection to add to')
      } else {
        submissionData.collectionUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.id + '_collection' + '/' + submissionData.version
        tempStr = submissionData.collectionUri.replace(config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/', '')
        submissionData.collectionId = submissionData.id + '_collection'
      }
    } else {
      tempStr = submissionData.collectionUri.replace(config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/', '')
      submissionData.collectionId = tempStr.substring(0, tempStr.indexOf('/')) + '_collection'
      submissionData.version = tempStr.replace(submissionData.collectionId + '/' + submissionData.collectionId + '_collection/', '')
    }
  }

  const citationRegEx = /^[0-9]+(,[0-9]*)*$/
  if (submissionData.citation && submissionData.citation.trim() !== '' &&
!citationRegEx.test(submissionData.citations)) {
    errors.push('Citations must be comma separated Pubmed IDs')
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

  if (!submissionData.filename) {
    submissionData.filename = tmp.fileSync().name
  }

  return [submissionData, errors]
}

async function handleSubmission (req, res, submission) {
  console.log(JSON.stringify(submission))

  let [submissionData, errors] = await sanitizeSubmission(submission)
  console.log(JSON.stringify(submissionData))

  if (errors.length > 0) {
    if (req.forceNoHTML || !req.accepts('text/html')) {
      res.status(400).type('text/plain').send(errors)
    } else {
      submitForm(req, res, submissionData, {
        errors: errors
      })
    }

    console.log('265')
    return
  }

  let graphUri = submissionData.createdBy.graphUri
  let uri = submissionData.collectionUri
  let metaData = await getCollectionMetaData(uri, graphUri)
  if (!metaData) {
    if (submissionData.overwriteMerge === '2' || submissionData.overwriteMerge === '3') {
      if (req.forceNoHTML || !req.accepts('text/html')) {
        res.status(400).type('text/plain').send('Submission id and version do not exist')
      } else {
        errors.push('Submission id and version do not exist')
        submitForm(req, res, submissionData, {
          errors: errors
        })
      }

      // Abandon the submission
      console.log('284')
      return
    }

    submissionData.overwriteMerge = '0'
  } else {
    if (submissionData.overwriteMerge === '2' || submissionData.overwriteMerge === '3') {
      // Merge
      console.log('merge')
      submissionData.id = metaData.displayId.replace('_collection', '')
      submissionData.version = metaData.version
      submissionData.name = metaData.name || ''
      submissionData.description = metaData.description || ''
    } else if (submissionData.overwriteMerge === '1') {
      // Overwrite
      console.log('overwrite')
    } else {
      // Prevent make public
      console.log('prevent')

      if (req.forceNoHTML || !req.accepts('text/html')) {
        console.log('prevent')
        res.status(400).type('text/plain').send('Submission id and version already in use')
      } else {
        errors.push('Submission id and version already in use')

        submitForm(req, res, submissionData, {
          errors: errors
        })
      }

      // Abandon the submission
      console.log('313')
      return
    }
  }

  console.log('Running submit plugin')
  let pluginResult = await submitPlugin(submissionData.plugin, submissionData.filename)
  submissionData.filename = pluginResult

  console.log('-- validating/converting')
  try {
    console.log(JSON.stringify({
      submit: true,
      uriPrefix: config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/',

      name: submissionData.name,
      description: submissionData.description,
      version: submissionData.version,

      rootCollectionIdentity: config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.collectionId + '/' + submissionData.version,
      newRootCollectionDisplayId: submissionData.collectionId,
      newRootCollectionVersion: submissionData.version,
      ownedByURI: config.get('databasePrefix') + 'user/' + submissionData.createdBy.username,
      creatorName: submissionData.createdBy.name,
      citationPubmedIDs: submissionData.citations,
      collectionChoices: submissionData.collectionChoices,
      overwrite_merge: submissionData.overwriteMerge
    }))

    console.log(JSON.stringify(submissionData))

    var { success, log, errorLog, resultFilename, attachmentFiles, extractDirPath } = await prepareSubmission(submissionData.filename, {
      submit: true,
      uriPrefix: config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/',

      name: submissionData.name,
      description: submissionData.description,
      version: submissionData.version,

      rootCollectionIdentity: config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.collectionId + '/' + submissionData.version,
      newRootCollectionDisplayId: submissionData.collectionId,
      newRootCollectionVersion: submissionData.version,
      ownedByURI: config.get('databasePrefix') + 'user/' + submissionData.createdBy.username,
      creatorName: submissionData.createdBy.name,
      citationPubmedIDs: submissionData.citations,
      collectionChoices: submissionData.collectionChoices,
      overwrite_merge: submissionData.overwriteMerge
    })
  } catch (err) {
    if (req.forceNoHTML || !req.accepts('text/html')) {
      res.status(500).type('text/plain').send(err)
      return
    } else {
      const locals = {
        config: config.get(),
        section: 'invalid',
        user: req.user,
        errors: [err]
      }

      res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
      return
    }
  }

  console.log(log)
  console.log(JSON.stringify(attachmentFiles))

  if (!success) {
    if (req.forceNoHTML || !req.accepts('text/html')) {
      res.status(400).type('text/plain').send(errorLog)
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

    await sparql.deleteStaggered(removeQuery, graphUri)

    templateParams = {
      uri: uri
    }

    removeQuery = loadTemplate('sparql/remove.sparql', templateParams)
    await sparql.deleteStaggered(removeQuery, graphUri)
  }

  await sparql.uploadFile(submissionData.createdBy.graphUri, resultFilename, 'application/rdf+xml')

  let baseURI = config.get('databasePrefix') + 'user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.collectionId
  let collectionURI = baseURI + '/' + submissionData.collectionId + '_collection/' + submissionData.version

  let sourceQuery = loadTemplate('sparql/GetAttachmentSourceFromTopLevel.sparql', { uri: collectionURI })

  let sourceResults = await sparql.queryJson(sourceQuery, submissionData.createdBy.graphUri)
  let sources = {}

  sourceResults.forEach(result => {
    var filename = result['source']
    uri = result['attachment']

    sources[filename] = uri
  })

  Object.keys(attachmentFiles).forEach(async filename => {
    if (attachmentFiles[filename] && attachmentFiles[filename].toLowerCase().indexOf('sbol') >= 0) { return }

    let fileStream = fs.createReadStream(filename)
    let { hash, size, mime } = await uploads.createUpload(fileStream)
    let originalFilename = filename

    if (filename.indexOf('/') >= 0) {
      filename = filename.substr(filename.lastIndexOf('/') + 1)
    }

    var key = 'file:' + filename

    if (sources[key]) {
      await attachments.updateAttachment(
        submissionData.createdBy.graphUri,
        sources[key],
        hash,
        size)

      return
    }

    let attachmentUri = await attachments.addAttachmentToTopLevel(
      submissionData.createdBy.graphUri,
      baseURI,
      collectionURI,
      filename,
      hash,
      size,
      attachmentFiles[originalFilename] || mime,
      submissionData.createdBy.username)

    console.log(attachmentUri)

    var badFileUri = 'file:' + filename
    var goodFileUri = attachmentUri

    let query = loadTemplate('./sparql/AttachmentUpdate.sparql', { oldUri: badFileUri, newUri: goodFileUri })
    await sparql.updateQuery(query, submissionData.createdBy.graphUri)
  })

  console.log('rm -r ' + extractDirPath)
  exec('rm -r ' + extractDirPath)

  console.log('unlinking:' + resultFilename)
  fs.unlink(resultFilename)

  if (req.forceNoHTML || !req.accepts('text/html')) {
    res.status(200).type('text/plain').send('Successfully uploaded')
  } else {
    res.redirect('/user/' + encodeURIComponent(submissionData.createdBy.username) + '/' + submissionData.id + '/' + submissionData.collectionId + '/' + submissionData.version)
  }
}
