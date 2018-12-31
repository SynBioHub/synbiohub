
var namespace = require('./namespace')

var summarizeIdentified = require('./summarizeIdentified')
var summarizeComponent = require('./summarizeComponent')
var summarizeLocation = require('./summarizeLocation')
var summarizeRoles = require('./summarizeRoles')

var URI = require('sboljs').URI

function summarizeSequenceConstraint(sequenceConstraint,req,sbol,remote,graphUri) {

    if (sequenceConstraint instanceof URI)
	return {
            uri: sequenceConstraint + '',
	    id: sequenceConstraint + ''
	}

    var definition
    
    if (sequenceConstraint.component) {
	definition = summarizeComponent(sequenceConstraint.component,req,sbol,remote,graphUri)
    }

    var summary = {
	subject: summarizeComponent(sequenceConstraint.subject,req,sbol,remote,graphUri),
	restriction: { url: sequenceConstraint.restriction.toString().replace('http://sbols.org','http://sbolstandard.org'),
		       id: sequenceConstraint.restriction.toString().replace('http://sbols.org/v2#','')
		     },
	object: summarizeComponent(sequenceConstraint.object,req,sbol,remote,graphUri)
    }

    summary = Object.assign(summary,summarizeIdentified(sequenceConstraint,req,sbol,remote,graphUri))

    return summary
}

module.exports = summarizeSequenceConstraint





