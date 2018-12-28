
var namespace = require('./namespace')

const uriToUrl = require('../../uriToUrl')

const shareImages = require('../../shareImages')

var wiky = require('../../wiky/wiky.js');

var URI = require('sboljs').URI

const attachments = require('../../attachments')

var filterAnnotations = require('../../filterAnnotations')

var config = require('../../config')

var sha1 = require('sha1');

function summarizeIdentified(identified,req) {

    if (identified instanceof URI)
	return {
            uri: identified + '',
	    id: identified + '',
	    name: identified
	}
    
    return {
        uri: identified.uri + '',
	url: url(identified,req),
        name: name(identified),
        id: id(identified),
        pid: pid(identified),
        version: version(identified),
        description: description(identified),
    }
}

function url(identified,req) {
    urlResult = '/' + identified.uri.toString().replace(config.get('databasePrefix'),'')
    if (req.url.toString().endsWith('/share')) {
	urlResult += '/' + sha1('synbiohub_' + sha1(identified.uri) + config.get('shareLinkSalt')) + '/share'
    }
    return urlResult;
}

function name(identified) {

    return identified.name || identified.displayId

}

function id(identified) {

    return identified.displayId /* or last fragment of URI? */

}

function pid(identified) {

    return identified.persistentIdentity

}

function version(identified) {

    return identified.version

}

function description(identified) {

    if(identified.description) {
	descriptionResult = wiky.process(identified.description, {})
	descriptionResult = descriptionResult.split(';').join('<br/>')
        return descriptionResult
    }

    return ''
}

module.exports = summarizeIdentified

