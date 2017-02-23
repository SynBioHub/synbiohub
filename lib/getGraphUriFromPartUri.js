
const config = require('./config')

function getGraphUriFromTopLevelUri(topLevelUri) {

    const databasePrefix = config.databasePrefix

    if(topLevelUri.indexOf(databasePrefix + 'public/') === 0) {

        return config.get('triplestore').defaultGraph

    }

    if(topLevelUri.indexOf(databasePrefix + 'user/' ) === 0) {

        return topLevelUri.slice((databasePrefix + 'user/').length).split('/')[0]

    }

    return config.get('triplestore').defaultGraph



}

module.exports = getGraphUriFromTopLevelUri


