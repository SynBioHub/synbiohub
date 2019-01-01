
var namespace = require('./namespace')
var summarizeTopLevel = require('./summarizeTopLevel')

var config = require('../config')

var URI = require('sboljs').URI

function summarizeCollection(collection,req,sbol,remote,graphUri) {

    if (collection instanceof URI)
	return {
            uri: collection + '',
	    id: collection + ''
	}

    return summarizeTopLevel(collection,req,sbol,remote,graphUri)
}

module.exports = summarizeCollection

