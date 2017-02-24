
const config = require('./config')

function getGraphUriFromTopLevelUri(topLevelUri,userUri) {

    const databasePrefix = config.get('databasePrefix')
    console.log('tl:'+topLevelUri)
    console.log(databasePrefix + 'public/'+ ':' + topLevelUri.indexOf(databasePrefix + 'public/'))
    if(topLevelUri.indexOf(databasePrefix + 'public/') === 0) {
	console.log('public')
        return config.get('triplestore').defaultGraph

    }

    console.log(userUri+ ':' + topLevelUri.indexOf(userUri))
    if(topLevelUri.indexOf(userUri) === 0) {
	console.log('user')
        return userUri

    }

    return config.get('triplestore').defaultGraph



}

module.exports = getGraphUriFromTopLevelUri


