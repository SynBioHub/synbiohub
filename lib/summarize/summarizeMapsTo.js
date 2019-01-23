var summarizeIdentified = require('./summarizeIdentified')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeMapsTo (mapsTo, req, sbol, remote, graphUri) {
  if (mapsTo instanceof URI) {
    return uriToMeta(mapsTo)
  }

  var summarizeComponentInstance = require('./summarizeComponentInstance')

  var local = summarizeComponentInstance(mapsTo.local, req, sbol, remote, graphUri)

  var remoteMapsTo = summarizeComponentInstance(mapsTo.remote, req, sbol, remote, graphUri)

  var summary = {
    local: local,
    refinement: { url: mapsTo.refinement.toString().replace('http://sbols.org', 'http://sbolstandard.org'),
      id: mapsTo.refinement.toString().replace('http://sbols.org/v2#', '')
    },
    remoteMapsTo: remoteMapsTo
  }

  summary = Object.assign(summary, summarizeIdentified(mapsTo, req, sbol, remote, graphUri))

  return summary
}

module.exports = summarizeMapsTo
