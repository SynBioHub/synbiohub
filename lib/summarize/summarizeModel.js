var summarizeTopLevel = require('./summarizeTopLevel')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var Model = require('sboljs/lib/Model')

var config = require('../config')

function summarizeModel (model, req, sbol, remote, graphUri) {
  if (model instanceof URI) {
    return uriToMeta(model)
  }
  if (!(model instanceof Model)) {
    return uriToMeta(model.uri)
  }

  var modelSourceName = model.source.toString().startsWith(config.get('databasePrefix')) ? 'Attachment' : model.source

  var summary = {
    modelSource: modelSource(model),
    modelSourceName: modelSourceName,
    framework: framework(model),
    language: language(model)
  }

  return Object.assign(summary, summarizeTopLevel(model, req, sbol, remote, graphUri))
}

function modelSource (model) {
  if (model.source.toString().startsWith(config.get('databasePrefix'))) {
    return '/' + model.source.toString().replace(config.get('databasePrefix'), '')
  }
  return model.source
}

function framework (model) {
  return {
    uri: model.framework,
    name: mapFramework(model.framework)
  }
}

function mapFramework (encoding) {
  return ({
    'http://identifiers.org/biomodels.sbo/SBO:0000004': 'Modeling Framework',
    'http://identifiers.org/biomodels.sbo/SBO:0000062': 'Continuous Framework',
    'http://identifiers.org/biomodels.sbo/SBO:0000293': 'Non-spatial Continuous Framework',
    'http://identifiers.org/biomodels.sbo/SBO:0000292': 'Spatial Continuous Framework',
    'http://identifiers.org/biomodels.sbo/SBO:0000063': 'Discrete Framework',
    'http://identifiers.org/biomodels.sbo/SBO:0000295': 'Non-spatial Discrete Framework',
    'http://identifiers.org/biomodels.sbo/SBO:0000294': 'Spatial Discrete Framework',
    'http://identifiers.org/biomodels.sbo/SBO:0000624': 'Flux Balance Framework',
    'http://identifiers.org/biomodels.sbo/SBO:0000234': 'Logical Framework',
    'http://identifiers.org/biomodels.sbo/SBO:0000547': 'Boolean Logical Framework'
  })[encoding]
}

function language (model) {
  return {
    uri: model.language,
    name: mapLanguage(model.language)
  }
}

function mapLanguage (encoding) {
  return ({
    'http://identifiers.org/edam/format_1915': 'Format',
    'http://identifiers.org/edam/format_2585': 'SBML',
    'http://identifiers.org/edam/format_3240': 'CellML',
    'http://identifiers.org/edam/format_3156': 'BioPAX'
  })[encoding]
}

module.exports = summarizeModel
