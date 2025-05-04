var summarizeIdentified = require('./summarizeIdentified')
var summarizeMapsTo = require('./summarizeMapsTo')
var summarizeMeasure = require('./summarizeMeasure')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeComponentInstance (componentInstance, req, sbol, remote, graphUri) {
  if (componentInstance instanceof URI) {
    return uriToMeta(componentInstance, req)
  }

  var summarizeComponentDefinition = require('./summarizeComponentDefinition')

  var definition = summarizeComponentDefinition(componentInstance.definition, req, sbol, remote, graphUri)

  var summary = {
    definition: definition,
    displayList: definition.displayList,
    access: { uri: componentInstance.access.toString(),
      url: componentInstance.access.toString(),
      id: componentInstance.access.toString().replace('http://sbols.org/v2#', '')
    },
    mapsTos: summarizeMapsTos(componentInstance, req, sbol, remote, graphUri),
    measures: summarizeMeasures(componentInstance, req, sbol, remote, graphUri)
  }

  summary = Object.assign(summary, summarizeIdentified(componentInstance, req, sbol, remote, graphUri))

  return summary
}

function summarizeMeasures (componentInstance, req, sbol, remote, graphUri) {
  var measures = []
  componentInstance.measures.forEach((measure) => {
    measures.push(summarizeMeasure(measure, req, sbol, remote, graphUri))
  })
  return measures
}

function summarizeMapsTos (componentInstance, req, sbol, remote, graphUri) {
  var mapsTos = []
  componentInstance.mappings.forEach((mapsTo) => {
    mapsTos.push(summarizeMapsTo(mapsTo, req, sbol, remote, graphUri))
  })
  return mapsTos
}

module.exports = summarizeComponentInstance
