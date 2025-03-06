var summarizeTopLevel = require('./summarizeTopLevel')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var GenericTopLevel = require('sboljs/lib/GenericTopLevel')

function summarizeGenericTopLevel (genericTopLevel, req, sbol, remote, graphUri) {
  if (genericTopLevel instanceof URI) {
    return uriToMeta(genericTopLevel, req)
  }
  if (!(genericTopLevel instanceof GenericTopLevel)) {
    return uriToMeta(genericTopLevel.uri, req)
  }

  return summarizeTopLevel(genericTopLevel, req, sbol, remote, graphUri)
}

module.exports = summarizeGenericTopLevel
