var summarizeTopLevel = require('./summarizeTopLevel')

var config = require('../config')
var sha1 = require('sha1')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var Attachment = require('sboljs/lib/Attachment')

function summarizeAttachment (attachment, req, sbol, remote, graphUri) {
  if (attachment instanceof URI) {
    return uriToMeta(attachment)
  }
  if (!(attachment instanceof Attachment)) {
    return uriToMeta(attachment.uri)
  }

  var summary = {
    attachmentType: attachment.format.toString().replace('http://identifiers.org/combine.specifications/', '').replace('http://identifiers.org/edam/', ''),
    attachmentHash: attachment.hash,
    attachmentName: 'Attachment',
    attachmentDownloadURL: attachmentDownloadURL(attachment, req),
    size: attachment.size,
    attachmentIsImage: attachment.format.toString().indexOf('png') >= 0 ||
attachment.format.toString() === 'http://identifiers.org/edam/format_3603' ||
attachment.format.toString().indexOf('imageAttachment') >= 0
  }

  return Object.assign(summary, summarizeTopLevel(attachment, req, sbol, remote, graphUri))
}

function attachmentDownloadURL (attachment, req) {
  var attachmentDownloadURLResult
  if (attachment.source.toString().startsWith(config.get('databasePrefix'))) {
    attachmentDownloadURLResult = '/' + attachment.source.toString().replace(config.get('databasePrefix'), '')
    if (attachment.source.toString().startsWith(config.get('databasePrefix') + 'user/') && req.url.toString().endsWith('/share')) {
      attachmentDownloadURLResult = attachmentDownloadURLResult.replace('/download', '') + '/' + sha1('synbiohub_' + sha1(attachment.source.toString().replace('/download', '')) + config.get('shareLinkSalt')) + '/share/download'
    }
  } else {
    attachmentDownloadURLResult = attachment.source
  }
  if (attachmentDownloadURLResult instanceof URI) {
    attachmentDownloadURLResult = attachmentDownloadURLResult.toString()
  }
  return attachmentDownloadURLResult
}

module.exports = summarizeAttachment
