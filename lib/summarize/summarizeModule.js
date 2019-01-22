var summarizeIdentified = require('./summarizeIdentified')
var summarizeMapsTo = require('./summarizeMapsTo')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeModule (module, req, sbol, remote, graphUri) {
  if (module instanceof URI) {
    return uriToMeta(module)
  }

  var summarizeModuleDefinition = require('./summarizeModuleDefinition')

  var definition = summarizeModuleDefinition(module.definition, req, sbol, remote, graphUri)

  var summary = {
    definition: definition,
    displayList: definition.displayList,
    mapsTos: summarizeMapsTos(module, req, sbol, remote, graphUri)
  }

  summary = Object.assign(summary, summarizeIdentified(module, req, sbol, remote, graphUri))

  return summary
}

function summarizeMapsTos (module, req, sbol, remote, graphUri) {
  var mapsTos = []
  module.mappings.forEach((mapsTo) => {
    mapsTos.push(summarizeMapsTo(mapsTo, req, sbol, remote, graphUri))
  })
  return mapsTos
}

module.exports = summarizeModule
