
const config = require('./config')

function getGraphUriFromTopLevelUri (topLevelUri, user) {
  const databasePrefix = config.get('databasePrefix')

  if (topLevelUri.indexOf(databasePrefix + 'public/') === 0) {
    return config.get('triplestore').defaultGraph
  }

  if (user && topLevelUri.indexOf(user.graphUri) === 0) {
    return user.graphUri
  } else if (topLevelUri.indexOf('user') !== -1) {
    let userPosition = topLevelUri.indexOf('/user/')
    let end = topLevelUri.indexOf('/', userPosition + 6)

    return topLevelUri.substring(0, end)
  }

  return config.get('triplestore').defaultGraph
}

module.exports = getGraphUriFromTopLevelUri
