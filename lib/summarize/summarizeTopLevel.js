const attachments = require('../attachments')

const summarizeIdentified = require('./summarizeIdentified')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeTopLevel (topLevel, req, sbol, remote, graphUri) {
  if (topLevel instanceof URI) {
    return uriToMeta(topLevel)
  }

  var summary = {
    attachments: attachments.getAttachmentsFromTopLevel(sbol, topLevel, req.url.toString().endsWith('/share'))
  }

  return Object.assign(summary, summarizeIdentified(topLevel, req, sbol, remote, graphUri))
}

module.exports = summarizeTopLevel
