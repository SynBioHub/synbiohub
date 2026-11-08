
const config = require('../config')
const splitUri = require('../splitUri')

const remote = {
  synbiohub: require('./remote/synbiohub/fetch-sbol-source'),
  ice: require('./remote/ice/fetch-sbol-source'),
  benchling: require('./remote/benchling/fetch-sbol-source')
}

const local = require('./local/fetch-sbol-source')

function fetchSBOLSource (type, uri, graphUri, authToken) {
  const args = [].slice.call(arguments, 0)

  /* fetchSBOLSource(uri, graphUri)
*/
  if (args.length === 2) {
    type = null
    uri = args[0]
    graphUri = args[1]
  } else if (args.length === 3) {
    if (typeof args[0] === 'string' && (args[0].startsWith('http://') || args[0].startsWith('https://'))) {
      type = null
      uri = args[0]
      graphUri = args[1]
      authToken = args[2]
    } else {
      type = args[0]
      uri = args[1]
      graphUri = args[2]
    }
  }

  const { submissionId, version } = splitUri(uri)
  const remoteConfig = config.get('remotes')[submissionId]

  return remoteConfig !== undefined && version === 'current'
    ? remote[remoteConfig.type].fetchSBOLSource(remoteConfig, type, uri)
    : local.fetchSBOLSource(type, uri, graphUri, authToken)
}

module.exports = {
  fetchSBOLSource: fetchSBOLSource
}
