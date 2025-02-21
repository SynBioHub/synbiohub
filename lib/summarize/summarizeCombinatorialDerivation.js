var summarizeTopLevel = require('./summarizeTopLevel')
var summarizeComponentDefinition = require('./summarizeComponentDefinition')
var summarizeVariableComponent = require('./summarizeVariableComponent')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var CombinatorialDerivation = require('sboljs/lib/CombinatorialDerivation')

function summarizeCombinatorialDerivation (combinatorialDerivation, req, sbol, remote, graphUri) {
  if (combinatorialDerivation instanceof URI) {
    return uriToMeta(combinatorialDerivation, req)
  }
  if (!(combinatorialDerivation instanceof CombinatorialDerivation)) {
    return uriToMeta(combinatorialDerivation.uri, req)
  }

  var template = summarizeComponentDefinition(combinatorialDerivation.template, req, sbol, remote, graphUri)

  var summary = {
    strategy: mapStrategy(combinatorialDerivation.strategy),
    template: template,
    displayList: template.displayList,
    variableComponents: summarizeVariableComponents(combinatorialDerivation, req, sbol, remote, graphUri)
  }
  return Object.assign(summary, summarizeTopLevel(combinatorialDerivation, req, sbol, remote, graphUri))
}

function summarizeVariableComponents (combinatorialDerivation, req, sbol, remote, graphUri) {
  var variableComponents = []
  combinatorialDerivation.variableComponents.forEach((variableComponent) => {
    variableComponents.push(summarizeVariableComponent(variableComponent, req, sbol, remote, graphUri))
  })
  return variableComponents
}

function mapStrategy (strategy) {
  return { uri: strategy.toString(),
    url: strategy.toString(),
    name: strategy.toString().replace('http://sbols.org/v2#', '')
  }
}

module.exports = summarizeCombinatorialDerivation
