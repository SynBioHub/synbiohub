
const config = require('./config')

function getGraphUriFromTopLevelUri (topLevelUri) {
  const databasePrefix = config.get('databasePrefix')
  const publicPrefix = databasePrefix + 'public/'
  const userPrefix = databasePrefix + 'user/'
  const defaultGraph = config.get('triplestore').defaultGraph

  if (topLevelUri.startsWith(publicPrefix)) {
    return defaultGraph
  } else if (topLevelUri.startsWith(userPrefix)) {
    let end = topLevelUri.indexOf('/', userPrefix.length)
    return topLevelUri.substring(0, end)
  } else {
    return defaultGraph
  }
}

module.exports = getGraphUriFromTopLevelUri
