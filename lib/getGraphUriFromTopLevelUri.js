
const config = require('./config')

function getGraphUriFromTopLevelUri(topLevelUri,userUri) {

    const databasePrefix = config.get('databasePrefix')
    if(topLevelUri.indexOf(databasePrefix + 'public/') === 0) {
        return config.get('triplestore').defaultGraph
    }

    if(topLevelUri.indexOf(userUri) === 0) {
        return userUri
    }

    return config.get('triplestore').defaultGraph



}

module.exports = getGraphUriFromTopLevelUri


