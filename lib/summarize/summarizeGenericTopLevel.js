var summarizeTopLevel = require('./summarizeTopLevel')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeGenericTopLevel (genericTopLevel, req, sbol, remote, graphUri) {
  if (genericTopLevel instanceof URI) {
    return uriToMeta(genericTopLevel)
  }

  return summarizeTopLevel(genericTopLevel, req, sbol, remote, graphUri)
}

module.exports = summarizeGenericTopLevel
