var sha1 = require('sha1');

const config = require('./config')

function uriToUrl(uri,req) {

    if (!uri) {
	return ''
    }

    if (uri.uri) {
	uri = uri.uri
    }

    if (req && req.url.toString().endsWith('/share')) {
        uri += '/' + sha1('synbiohub_' + sha1(uri.toString()) + config.get('shareLinkSalt')) + '/share'
    }

    if(uri.toString().indexOf(config.get('databasePrefix')) === 0) {

        return '/' + uri.toString().slice(config.get('databasePrefix').length)

    }

    return uri

}

module.exports = uriToUrl

