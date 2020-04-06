var summarizeComponentInstance = require('./summarizeComponentInstance')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var FunctionalComponent = require('sboljs/lib/FunctionalComponent')

function summarizeFunctionalComponent (functionalComponent, req, sbol, remote, graphUri) {
  if (functionalComponent instanceof URI) {
    return uriToMeta(functionalComponent)
  }
  if (!(functionalComponent instanceof FunctionalComponent)) {
    return uriToMeta(functionalComponent.uri)
  }

  var summary = {
    direction: { uri: functionalComponent.direction.toString(),
      url: functionalComponent.direction.toString(),
      id: functionalComponent.direction.toString().replace('http://sbols.org/v2#', '')
    }
  }

  summary = Object.assign(summary, summarizeComponentInstance(functionalComponent, req, sbol, remote, graphUri))

  return summary
}

module.exports = summarizeFunctionalComponent
