var summarizeComponentInstance = require('./summarizeComponentInstance')
var summarizeLocation = require('./summarizeLocation')
var summarizeRoles = require('./summarizeRoles')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var Component = require('sboljs/lib/Component')

function summarizeComponent (component, req, sbol, remote, graphUri) {
  if (component instanceof URI) {
    return uriToMeta(component, req)
  }
  if (!(component instanceof Component)) {
    return uriToMeta(component.uri, req)
  }

  var summary = {
    locations: summarizeLocations(component, req, sbol, remote, graphUri),
    sourceLocations: summarizeSourceLocations(component, req, sbol, remote, graphUri),
    roles: summarizeRoles(component),
    roleIntegration: { uri: component.roleIntegration.toString(),
      url: component.roleIntegration.toString(),
      id: component.roleIntegration.toString().replace('http://sbols.org/v2#', '')
    }
  }

  summary = Object.assign(summary, summarizeComponentInstance(component, req, sbol, remote, graphUri))

  return summary
}

function summarizeLocations (component, req, sbol, remote, graphUri) {
  var locations = []
  component.locations.forEach((location) => {
    locations.push(summarizeLocation(location, req, sbol, remote, graphUri))
  })
  return locations
}

function summarizeSourceLocations (component, req, sbol, remote, graphUri) {
  var locations = []
  component.sourceLocations.forEach((location) => {
    locations.push(summarizeLocation(location, req, sbol, remote, graphUri))
  })
  return locations
}

module.exports = summarizeComponent
