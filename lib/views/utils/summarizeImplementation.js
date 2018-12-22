
var namespace = require('./namespace')
var summarizeGenericTopLevel = require('./summarizeGenericTopLevel')

var config = require('../../config')

var URI = require('sboljs').URI

function summarizeImplementation(implementation,req,sbol,remote,graphUri) {

    if (implementation instanceof URI)
	return {
            uri: implementation + '',
	    id: implementation + ''
	}

    var summary = {
        built: { url : implementation.built.uri.toString(),
		 name : implementation.built.name
	       }
    }

    return Object.assign(summary,summarizeGenericTopLevel(implementation,req,sbol,remote,graphUri))
}

module.exports = summarizeImplementation

