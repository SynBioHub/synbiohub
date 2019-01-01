var summarizeIdentified = require('./summarizeIdentified')
var summarizeTopLevel = require('./summarizeTopLevel')
var summarizeRoles = require('./summarizeRoles')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeAssociation(association,req,sbol,remote,graphUri) {

    if (association instanceof URI) {
	return uriToMeta(association)
    }

    var plan
    if (association.plan) {
	plan = summarizeTopLevel(association.plan,req,sbol,remote,graphUri)
    }
    
    var summary = {
	roles: summarizeRoles(association),
	agent: summarizeTopLevel(association.agent,req,sbol,remote,graphUri),
	plan: plan
    }

    summary = Object.assign(summary,summarizeIdentified(association,req,sbol,remote,graphUri))

    return summary
}

module.exports = summarizeAssociation





