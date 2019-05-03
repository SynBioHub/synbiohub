var summarizeTopLevel = require('./summarizeTopLevel')
var summarizeGenericTopLevel = require('./summarizeGenericTopLevel')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var Experiment = require('sboljs/lib/Experiment')

function summarizeExperiment (experiment, req, sbol, remote, graphUri) {
  if (experiment instanceof URI) {
    return uriToMeta(experiment)
  }
  if (!(experiment instanceof Experiment)) {
    return uriToMeta(experiment.uri)
  }

  var summary = {
    experimentalData: summarizeExperimentalData(experiment, req, sbol, remote, graphUri)
  }
  return Object.assign(summary, summarizeTopLevel(experiment, req, sbol, remote, graphUri))
}

function summarizeExperimentalData (experiment, req, sbol, remote, graphUri) {
  var experimentalData = []
  experiment.experimentalData.forEach((experimentalDatum) => {
    experimentalData.push(summarizeGenericTopLevel(experimentalDatum, req, sbol, remote, graphUri))
  })
  return experimentalData
}

module.exports = summarizeExperiment
