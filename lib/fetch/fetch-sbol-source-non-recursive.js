
const config = require('../config')
const splitUri = require('../splitUri')

const remote = {
  synbiohub: require('./remote/synbiohub/fetch-sbol-source'),
  ice: require('./remote/ice/fetch-sbol-source'),
  benchling: require('./remote/benchling/fetch-sbol-source')
}

const local = require('./local/fetch-sbol-source-non-recursive')

function fetchSBOLSourceNonRecursive (type, uri, graphUri) {
  const args = [].slice.call(arguments, 0)

  /* fetchSBOLSource(uri, graphUri)
*/
  if (args.length === 2) {
    type = null
    uri = args[0]
    graphUri = args[1]
  }

  const { submissionId, version } = splitUri(uri)
  const remoteConfig = config.get('remotes')[submissionId]

  return remoteConfig !== undefined && version === 'current'
    ? remote[remoteConfig.type].fetchSBOLSource(remoteConfig, type, uri)
    : local.fetchSBOLSourceNonRecursive(type, uri, graphUri, true)
}

module.exports = {
  fetchSBOLSourceNonRecursive: fetchSBOLSourceNonRecursive
}
