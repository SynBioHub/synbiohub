
const config = require('./config')

function getGraphUriFromTopLevelUri(topLevelUri,user) {

    const databasePrefix = config.get('databasePrefix')

    if(topLevelUri.indexOf(databasePrefix + 'public/') === 0) {
        return config.get('triplestore').defaultGraph
    }

    //if(user && topLevelUri.indexOf(user.graphUri) === 0) {
    if (user) {
        return user.graphUri
    }
    //}

    if (topLevelUri.endsWith('/share')) {
	//var uri
    }

    return config.get('triplestore').defaultGraph



}

module.exports = getGraphUriFromTopLevelUri


