var summarizeIdentified = require('./summarizeIdentified')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var summarizeSequence = require('./summarizeSequence')

function summarizeLocation (location, req, sbol, remote, graphUri) {
  if (location instanceof URI) {
    return uriToMeta(location)
  }

  var locationStr = ''
  if (location.orientation.toString() === 'http://sbols.org/v2#reverseComplement') {
    locationStr = 'complement('
  }
  if (location.start) {
    locationStr += location.start + ',' + location.end
  } else if (location.at) {
    var atVal = location.at + 1
    locationStr += location.at + '^' + atVal
  } else {
    locationStr += 'generic'
  }
  if (location.orientation.toString() === 'http://sbols.org/v2#reverseComplement') {
    locationStr += ')'
  }

  var summary = {
    locationStr: locationStr,
    sequence: summarizeSequence(location.sequence, req, sbol, remote, graphUri)
  }

  summary = Object.assign(summary, summarizeIdentified(location, req, sbol, remote, graphUri))

  return summary
}

module.exports = summarizeLocation
