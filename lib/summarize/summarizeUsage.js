var summarizeIdentified = require('./summarizeIdentified')
var summarizeTopLevel = require('./summarizeTopLevel')
var summarizeRoles = require('./summarizeRoles')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var ProvUsage = require('sboljs/lib/ProvUsage')

function summarizeUsage (usage, req, sbol, remote, graphUri) {
  if (usage instanceof URI) {
    return uriToMeta(usage, req)
  }
  if (!(usage instanceof ProvUsage)) {
    return uriToMeta(usage.uri, req)
  }

  var summary = {
    roles: summarizeRoles(usage),
    entity: summarizeTopLevel(usage.entity, req, sbol, remote, graphUri)
  }

  summary = Object.assign(summary, summarizeIdentified(usage, req, sbol, remote, graphUri))

  return summary
}

module.exports = summarizeUsage
