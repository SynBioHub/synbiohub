var summarizeIdentified = require('./summarizeIdentified')
var summarizeTopLevel = require('./summarizeTopLevel')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var Implementation = require('sboljs/lib/Implementation')

function summarizeImplementation (implementation, req, sbol, remote, graphUri) {
  if (implementation instanceof URI) {
    return uriToMeta(implementation)
  }
  if (!(implementation instanceof Implementation)) {
    return uriToMeta(implementation.uri)
  }

  var built
  if (implementation.built) {
    built = summarizeIdentified(implementation.built, req)
  }
  var summary = {
    built: built
  }

  return Object.assign(summary, summarizeTopLevel(implementation, req, sbol, remote, graphUri))
}

module.exports = summarizeImplementation
