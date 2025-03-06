var summarizeIdentified = require('./summarizeIdentified')
var summarizeComponent = require('./summarizeComponent')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var SequenceConstraint = require('sboljs/lib/SequenceConstraint')

function summarizeSequenceConstraint (sequenceConstraint, req, sbol, remote, graphUri) {
  if (sequenceConstraint instanceof URI) {
    return uriToMeta(sequenceConstraint, req)
  }
  if (!(sequenceConstraint instanceof SequenceConstraint)) {
    return uriToMeta(sequenceConstraint.uri, req)
  }

  var summary = {
    subject: summarizeComponent(sequenceConstraint.subject, req, sbol, remote, graphUri),
    restriction: { url: sequenceConstraint.restriction.toString(),
      id: sequenceConstraint.restriction.toString().replace('http://sbols.org/v2#', '')
    },
    object: summarizeComponent(sequenceConstraint.object, req, sbol, remote, graphUri)
  }

  summary = Object.assign(summary, summarizeIdentified(sequenceConstraint, req, sbol, remote, graphUri))

  return summary
}

module.exports = summarizeSequenceConstraint
