
var SBOLDocument = require('sboljs')

var assert = require('assert')

const config = require('../config')
const splitUri = require('../splitUri')

const local = require('./local/fetch-sbol-object-non-recursive')

const remote = {
  synbiohub: require('./remote/synbiohub/fetch-sbol-object-non-recursive'),
  ice: require('./remote/ice/fetch-sbol-object-non-recursive'),
  benchling: require('./remote/benchling/fetch-sbol-object-non-recursive')
}

function fetchSBOLObjectNonRecursive (uri, graphUri) {
  var sbol = new SBOLDocument()
  var type = null

  if (Array.isArray(uri)) {
    assert(uri.length === 1)
    uri = uri[0]
  }

  const { submissionId, version } = splitUri(uri)
  const remoteConfig = config.get('remotes')[submissionId]

  return remoteConfig !== undefined && version === 'current'
    ? remote[remoteConfig.type].fetchSBOLObjectNonRecursive(remoteConfig, sbol, type, uri)
    : local.fetchSBOLObjectNonRecursive(sbol, type, uri, graphUri, false)
}

module.exports = {
  fetchSBOLObjectNonRecursive: fetchSBOLObjectNonRecursive
}
