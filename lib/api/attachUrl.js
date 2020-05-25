const getUrisFromReq = require('../getUrisFromReq')
const loadTemplate = require('../loadTemplate')
const sparql = require('../sparql/sparql')
const uuid = require('uuid/v4')
const getOwnedBy = require('../query/ownedBy')

async function serve (req, res) {
  let { graphUri, uri, url, baseUri } = getUrisFromReq(req, res)
  let source = req.body.url
  let typeString = req.body.type
  let name = req.body.name || uuid()

  let displayId = name
  let version = '1'
  let attachmentUri = `${baseUri}/${displayId}/${version}`
  let persistentIdentity = `${baseUri}/${displayId}`
  let ownedBy = await getOwnedBy(uri, graphUri)
  ownedBy = ownedBy[0]

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
    type: typeString
  })

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
