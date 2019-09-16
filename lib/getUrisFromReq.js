const config = require('./config')

function getUrisFromReq (req) {
  var uri
  var url
  var baseUri
  var baseUrl

  let graphUri = null
  if (req.params.userId) {
    graphUri = config.get('instanceUrl') + 'user/' + req.params.userId
  }

  let designId = req.params.collectionId + '/' + req.params.displayId
  if (req.params.version) {
    designId = designId + '/' + req.params.version
  }

  if (req.params.userId) {
    url = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId
    baseUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + req.params.collectionId
    baseUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + req.params.collectionId
    uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
  } else {
    url = '/public/' + designId
    baseUrl = '/public/' + req.params.collectionId
    baseUri = config.get('databasePrefix') + 'public/' + req.params.collectionId
    uri = config.get('databasePrefix') + 'public/' + designId
  }

  return {
    graphUri: graphUri,
    uri: uri,
    designId: designId,
    share: false,
    url: url,
    baseUrl: baseUrl,
    baseUri: baseUri,
    collectionId: req.params.collectionId,
    version: req.params.version,
    edit: false
  }
}

module.exports = getUrisFromReq
