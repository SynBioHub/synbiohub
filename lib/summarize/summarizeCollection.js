var summarizeTopLevel = require('./summarizeTopLevel')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var Collection = require('sboljs/lib/Collection')

function summarizeCollection (collection, req, sbol, remote, graphUri) {
  if (collection instanceof URI) {
    return uriToMeta(collection, req)
  }
  if (!(collection instanceof Collection)) {
    return uriToMeta(collection.uri, req)
  }
  return summarizeTopLevel(collection, req, sbol, remote, graphUri)
}

module.exports = summarizeCollection
