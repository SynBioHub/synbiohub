
var SBOLDocument = require('sboljs')

var assert = require('assert')

const config = require('../config')
const splitUri = require('../splitUri')

const local = require('./local/fetch-sbol-object-recursive')

const remote = {
  synbiohub: require('./remote/synbiohub/fetch-sbol-object-recursive'),
  ice: require('./remote/ice/fetch-sbol-object-recursive'),
  benchling: require('./remote/benchling/fetch-sbol-object-recursive')
}

function fetchSBOLObjectRecursive (sbol, type, uri, graphUri, authToken) {
  const args = [].slice.call(arguments, 0)

  /* fetchSBOLObjectRecursive(uri, graphUri)
*/
  if (args.length === 2) {
    sbol = new SBOLDocument()
    type = null
    uri = args[0]
    graphUri = args[1]
  } else if (args.length === 3) {
    if (typeof args[0] === 'string' && (args[0].startsWith('http://') || args[0].startsWith('https://'))) {
      sbol = new SBOLDocument()
      type = null
      uri = args[0]
      graphUri = args[1]
      authToken = args[2]
    } else {
      /* fetchSBOLObjectRecursive(type, uri, graphUri)
*/
      sbol = new SBOLDocument()
      type = args[0]
      uri = args[1]
      graphUri = args[2]
    }
  } else if (args.length >= 4 && args[0] && typeof args[0].lookupURI === 'function') {
    sbol = args[0]
    type = args[1]
    uri = args[2]
    graphUri = args[3]
    authToken = args[4] || authToken
  }

  if (Array.isArray(uri)) {
    assert(uri.length === 1)
    uri = uri[0]
  }

  const { submissionId, version } = splitUri(uri)
  const remoteConfig = config.get('remotes')[submissionId]

  return remoteConfig !== undefined && version === 'current'
    ? remote[remoteConfig.type].fetchSBOLObjectRecursive(remoteConfig, sbol, type, uri)
    : local.fetchSBOLObjectRecursive(sbol, type, uri, graphUri, authToken)
}

module.exports = {
  fetchSBOLObjectRecursive: fetchSBOLObjectRecursive
}
