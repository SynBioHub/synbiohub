var summarizeIdentified = require('./summarizeIdentified')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var systemsBiologyOntology = require('../ontologies/systems-biology-ontology')

function summarizeMeasure (measure, req, sbol, remote, graphUri) {
  if (measure instanceof URI) {
    return uriToMeta(measure)
  }

  var summary = {
    value: measure.value,
    unit: uriToMeta(measure.unit),
    types: summarizeTypes(measure)
  }

  summary = Object.assign(summary, summarizeIdentified(measure, req, sbol, remote, graphUri))

  return summary
}

function summarizeTypes (measure) {
  var types = []
  var typeResult
  measure.types.forEach((type) => {
    var sboPrefix = 'http://identifiers.org/biomodels.sbo/'
    if (type.toString().indexOf(sboPrefix) === 0) {
      var sboTerm = type.toString().slice(sboPrefix.length).split('_').join(':')
      typeResult = { uri: type.toString(),
        term: type.toString(),
        description: systemsBiologyOntology[sboTerm]
      }
    } else {
      typeResult = { uri: type.toString()
      }
    }
    types.push(typeResult)
  })
  return types
}

module.exports = summarizeMeasure
