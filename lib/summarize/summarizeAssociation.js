var summarizeIdentified = require('./summarizeIdentified')
var summarizeTopLevel = require('./summarizeTopLevel')
var summarizeRoles = require('./summarizeRoles')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var ProvAssociation = require('sboljs/lib/ProvAssociation')

function summarizeAssociation (association, req, sbol, remote, graphUri) {
  if (association instanceof URI) {
    return uriToMeta(association)
  }
  if (!(association instanceof ProvAssociation)) {
    return uriToMeta(association.uri)
  }

  var plan
  if (association.plan) {
    plan = summarizeTopLevel(association.plan, req, sbol, remote, graphUri)
  }

  var summary = {
    roles: summarizeRoles(association),
    agent: summarizeTopLevel(association.agent, req, sbol, remote, graphUri),
    plan: plan
  }

  summary = Object.assign(summary, summarizeIdentified(association, req, sbol, remote, graphUri))

  return summary
}

module.exports = summarizeAssociation
