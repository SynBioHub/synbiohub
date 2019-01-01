var summarizeTopLevel = require('./summarizeTopLevel')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeCollection(collection,req,sbol,remote,graphUri) {
    if (collection instanceof URI) {
	return uriToMeta(collection)
    }
    return summarizeTopLevel(collection,req,sbol,remote,graphUri)
}

module.exports = summarizeCollection

