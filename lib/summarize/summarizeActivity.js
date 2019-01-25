
var namespace = require('./namespace')
var summarizeTopLevel = require('./summarizeTopLevel')
var summarizeUsage = require('./summarizeUsage')
var summarizeAssociation = require('./summarizeAssociation')

var config = require('../config')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeActivity (activity, req, sbol, remote, graphUri) {
  if (activity instanceof URI) {
    return uriToMeta(activity)
  }

  var summary = {
    startedAtTime: summarizeDateTime(activity.startedAtTime),
    endedAtTime: summarizeDateTime(activity.endedAtTime),
    usages: summarizeUsages(activity, req, sbol, remote, graphUri),
    associations: summarizeAssociations(activity, req, sbol, remote, graphUri)
  }

  return Object.assign(summary, summarizeTopLevel(activity, req, sbol, remote, graphUri))
}

function summarizeDateTime (dateTime) {
  if (dateTime && dateTime != '') {
    dateTime = dateTime.toISOString().split('Z')[0]
    return dateTime.replace('T', ' ').replace('Z', '')
  }
  return null
}

function summarizeUsages (activity, req, sbol, remote, graphUri) {
  var usages = []
  activity.usages.forEach((usage) => {
    usages.push(summarizeUsage(usage, req, sbol, remote, graphUri))
  })
  return usages
}

function summarizeAssociations (activity, req, sbol, remote, graphUri) {
  var associations = []
  activity.associations.forEach((association) => {
    associations.push(summarizeAssociation(association, req, sbol, remote, graphUri))
  })
  return associations
}

module.exports = summarizeActivity
