var summarizeIdentified = require('./summarizeIdentified')
var summarizeComponent = require('./summarizeComponent')
var summarizeLocation = require('./summarizeLocation')
var summarizeRoles = require('./summarizeRoles')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var SequenceAnnotation = require('sboljs/lib/SequenceAnnotation')

function summarizeSequenceAnnotation (sequenceAnnotation, req, sbol, remote, graphUri) {
  if (sequenceAnnotation instanceof URI) {
    return uriToMeta(sequenceAnnotation, req)
  }
  if (!(sequenceAnnotation instanceof SequenceAnnotation)) {
    return uriToMeta(sequenceAnnotation.uri, req)
  }

  var definition

  if (sequenceAnnotation.component) {
    definition = summarizeComponent(sequenceAnnotation.component, req, sbol, remote, graphUri)
  }

  var summary = {
    component: definition,
    roles: summarizeRoles(sequenceAnnotation),
    locations: summarizeLocations(sequenceAnnotation, req, sbol, remote, graphUri)
  }

  summary = Object.assign(summary, summarizeIdentified(sequenceAnnotation, req, sbol, remote, graphUri))

  return summary
}

function summarizeLocations (sequenceAnnotation, req, sbol, remote, graphUri) {
  var locations = []
  sequenceAnnotation.locations.forEach((location) => {
    locations.push(summarizeLocation(location, req, sbol, remote, graphUri))
  })
  return locations
}

module.exports = summarizeSequenceAnnotation
