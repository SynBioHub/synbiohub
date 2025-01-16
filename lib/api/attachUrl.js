const getUrisFromReq = require('../getUrisFromReq')
const loadTemplate = require('../loadTemplate')
const sparql = require('../sparql/sparql')
const uuid = require('uuid/v4')
const getOwnedBy = require('../query/ownedBy')
const config = require('../config')

async function serve (req, res) {
  let { graphUri, uri, url, baseUri } = getUrisFromReq(req, res)
  let source = req.body.url
  let typeString = req.body.type
  let name = req.body.name || uuid()

  // Replace non-alphanumeric characters and spaces with underscores
  let displayId = name.replace(/[^a-zA-Z0-9]/g, '_')
  // Ensure it does not start with a number by prefixing an underscore if needed
  if (/^\d/.test(displayId)) {
    displayId = '_' + displayId
  }

  let version = '1'
  let attachmentUri = `${baseUri}/${displayId}/${version}`
  let persistentIdentity = `${baseUri}/${displayId}`
  let ownedBy = await getOwnedBy(uri, graphUri)
  const collectionUri = baseUri + baseUri.slice(baseUri.lastIndexOf('/')) + '_collection/' + version

  if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
    if (!req.accepts('text/html')) {
      res.status(401).send('Not authorized to add attachments to this submission')
    } else {
      res.redirect(url)
    }
    return
  }

  if (!source) {
    // error
  }

  if (!typeString) {
    // error
  }

  if (typeString === 'Pick an attachment type...') {
    if (!req.accepts('text/html')) {
      res.status(400).send('Must pick an attachment type')
    } else {
      res.redirect(url)
    }
    return
  }

  const query = loadTemplate('./sparql/AttachUrl.sparql', {
    name: name,
    displayId: displayId,
    version: version,
    topLevel: uri,
    persistentIdentity: persistentIdentity,
    ownedBy: ownedBy,
    uri: attachmentUri,
    source: source,
    type: typeString,
    collectionUri: collectionUri
  })

  console.log('attachUrl: ' + graphUri)
  console.log('attachUrl: ' + query)

  sparql.updateQueryJson(query, graphUri).then(() => {
    if (!req.accepts('text/html')) {
      res.status(200).type('text/plain').send('Success')
    } else {
      res.redirect(url)
    }
  }).catch((err) => {
    if (!req.accepts('text/html')) {
      res.status(500).send(err)
    } else {
      res.redirect(url)
    }
  })
}

module.exports = serve
