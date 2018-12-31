
var namespace = require('./namespace')
var summarizeIdentified = require('./summarizeIdentified')
var summarizeTopLevel = require('./summarizeTopLevel')

var config = require('../config')

var URI = require('sboljs').URI

function summarizeImplementation(implementation,req,sbol,remote,graphUri) {

    if (implementation instanceof URI)
	return {
            uri: implementation + '',
	    id: implementation + ''
	}

    var summary = {
        built: summarizeIdentified(implementation.built,req)
    }

    return Object.assign(summary,summarizeTopLevel(implementation,req,sbol,remote,graphUri))
}

module.exports = summarizeImplementation

