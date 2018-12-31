
var namespace = require('./namespace')
var summarizeTopLevel = require('./summarizeTopLevel')

var config = require('../config')

var URI = require('sboljs').URI

function summarizeGenericTopLevel(genericTopLevel,req,sbol,remote,graphUri) {

    if (genericTopLevel instanceof URI)
	return {
            uri: genericTopLevel + '',
	    id: genericTopLevel + ''
	}

    return summarizeTopLevel(genericTopLevel,req,sbol,remote,graphUri)
}

module.exports = summarizeGenericTopLevel

