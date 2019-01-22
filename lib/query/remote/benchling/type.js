
const splitUri = require('../../../splitUri')

function getType (remoteConfig, uri) {
  const { displayId } = splitUri(uri)

  if (displayId === remoteConfig.rootCollection.displayId) {
    return Promise.resolve('http://sbols.org/v2#Collection')
  } else if (displayId.indexOf(remoteConfig.folderPrefix) === 0) {
    return Promise.resolve('http://sbols.org/v2#Collection')
  } else if (displayId.endsWith(remoteConfig.sequenceSuffix)) {
    return Promise.resolve('http://sbols.org/v2#Sequence')
  } else {
    return Promise.resolve('http://sbols.org/v2#ComponentDefinition')
  }
}

module.exports = {
  getType: getType
}
