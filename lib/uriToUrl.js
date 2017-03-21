
const config = require('./config')

function uriToUrl(uri) {

    if(uri.indexOf(config.get('databasePrefix')) === 0) {

        return '/' + uri.slice(config.get('databasePrefix').length)

    }

    return uri

}

module.exports = uriToUrl

