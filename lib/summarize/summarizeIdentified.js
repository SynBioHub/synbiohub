var namespace = require('./namespace')
var privileges = require('../auth/privileges')
const uriToUrl = require('../uriToUrl')
const shareImages = require('../shareImages')
var wiky = require('../wiky/wiky.js')
var filterAnnotations = require('../filterAnnotations')
var config = require('../config')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var Identified = require('sboljs/lib/Identified')

function summarizeIdentified (identified, req, sbol, remote, graphUri) {
  if (identified instanceof URI) {
    return uriToMeta(identified)
  }
  if (!(identified instanceof Identified)) {
    return uriToMeta(identified.uri)
  }

  var mutableDescriptionSource = identified.getAnnotations(namespace.synbiohub + 'mutableDescription').toString() || ''
  var mutableNotesSource = identified.getAnnotations(namespace.synbiohub + 'mutableNotes').toString() || ''
  var sourceSource = identified.getAnnotations(namespace.synbiohub + 'mutableProvenance').toString() || ''

  return {
    uri: identified.uri + '',
    url: uriToUrl(identified, req).toString(),
    name: name(identified),
    id: id(identified),
    pid: pid(identified),
    version: version(identified),
    wasDerivedFrom: wasDerivedFrom(identified, req),
    wasDerivedFroms: wasDerivedFroms(identified, req),
    wasGeneratedBy: wasGeneratedBy(identified, req),
    wasGeneratedBys: wasGeneratedBys(identified, req),
    creator: creator(identified),
    created: created(identified),
    modified: modified(identified),
    mutableDescriptionSource: mutableDescriptionSource,
    mutableDescription: mutableDescription(mutableDescriptionSource, req),
    mutableNotesSource: mutableNotesSource,
    mutableNotes: mutableNotes(mutableNotesSource, req),
    sourceSource: sourceSource,
    source: source(sourceSource, req),
    description: description(identified),
    comments: comments(identified),
    labels: labels(identified),
    canEdit: canEdit(identified, req, remote),
    canUpdate: canUpdate(identified, req, remote),
    canShare: canShare(identified, req, remote),
    annotations: filterAnnotations(req, identified.annotations),
    triplestore: graphUri ? 'private' : 'public',
    graphUri: graphUri,
    remote: remote
  }
}

function name (identified) {
  return identified.name || identified.displayId
}

function id (identified) {
  return identified.displayId /* or last fragment of URI? */
}

function pid (identified) {
  return identified.persistentIdentity
}

function version (identified) {
  return identified.version
}

function wasDerivedFrom (identified, req) {
  var wasDerivedFromResult
  if (identified.wasDerivedFrom) {
    wasDerivedFromResult = { uri: identified.wasDerivedFrom.uri ? identified.wasDerivedFrom.uri : identified.wasDerivedFrom,
      url: uriToUrl(identified.wasDerivedFrom, req)
    }
  }
  return wasDerivedFromResult
}

function wasDerivedFroms (identified, req) {
  var wasDerivedFromsResult
  if (identified.wasDerivedFroms) {
    wasDerivedFromsResult = identified.wasDerivedFroms.map((wasDerivedFrom) => {
      return { uri: wasDerivedFrom.uri ? wasDerivedFrom.uri : wasDerivedFrom,
        url: uriToUrl(wasDerivedFrom, req)
      }
    })
  }
  return wasDerivedFromsResult
}

function wasGeneratedBy (identified, req) {
  var wasGeneratedByResult
  if (identified.wasGeneratedBy) {
    wasGeneratedByResult = { uri: identified.wasGeneratedBy.uri ? identified.wasGeneratedBy.uri : identified.wasGeneratedBy,
      url: uriToUrl(identified.wasGeneratedBy, req)
    }
  }
  return wasGeneratedByResult
}

function wasGeneratedBys (identified, req) {
  var wasGeneratedBysResult
  if (identified.wasGeneratedBys) {
    wasGeneratedBysResult = identified.wasGeneratedBys.map((wasGeneratedBy) => {
      return { uri: wasGeneratedBy.uri ? wasGeneratedBy.uri : wasGeneratedBy,
        url: uriToUrl(wasGeneratedBy, req)
      }
    })
  }
  return wasGeneratedBysResult
}

function description (identified) {
  var descriptionResult
  if (identified.description) {
    descriptionResult = wiky.process(identified.description, {})
    descriptionResult = descriptionResult.split(';').join('<br/>')
    return descriptionResult
  }

  var commentAnnotations = comments(identified)

  if (commentAnnotations) { return commentAnnotations.join('\n') }

  return ''
}

function creator (identified) {
  var creatorStr = identified.getAnnotations(namespace.dcterms + 'creator')
  if (creatorStr.toString() === '') {
    creatorStr = identified.getAnnotations(namespace.dc + 'creator')
  }
  return {
    description: creatorStr
  }
}

function created (identified) {
  var resultStr = identified.getAnnotations(namespace.dcterms + 'created')
  if (resultStr) {
    resultStr = resultStr.toString().split('Z')[0]
  }
  return {
    name: resultStr,
    description: resultStr.toString().replace('T', ' ').replace('Z', '')
  }
}

function modified (identified) {
  var resultStr = identified.getAnnotations(namespace.dcterms + 'modified')
  if (resultStr) {
    resultStr = resultStr.toString().split('Z')[0]
  }
  return {
    name: resultStr,
    description: resultStr.toString().replace('T', ' ').replace('Z', '')
  }
}

function mutableDescription (mutableDescriptionSource, req) {
  var mutableDescriptionResult = ''
  if (mutableDescriptionSource !== '') {
    mutableDescriptionResult = shareImages(req, mutableDescriptionSource)
    mutableDescriptionResult = wiky.process(mutableDescriptionSource, {})
  }
  return mutableDescriptionResult
}

function mutableNotes (mutableNotesSource, req) {
  var mutableNotesResult = ''
  if (mutableNotesSource !== '') {
    mutableNotesResult = shareImages(req, mutableNotesSource)
    mutableNotesResult = wiky.process(mutableNotesSource, {})
  }
  return mutableNotesResult
}

function source (sourceSource, req) {
  var sourceResult = ''
  if (sourceSource !== '') {
    sourceResult = shareImages(req, sourceSource)
    sourceResult = wiky.process(sourceSource, {})
  }
  return sourceResult
}

function labels (identified) {
  return identified.getAnnotations(namespace.rdfs + 'label').concat(identified.getAnnotations(namespace.dcterms + 'alternative'))
}

function comments (identified) {
  return identified.getAnnotations(namespace.rdfs + 'comment')
}

function canEdit (identified, req, remote) {
  if (!remote && req.privilege) {
    return privileges.canEdit(req.privilege)
  }

  return false
}

function canUpdate (identified, req, remote) {
  let publicPrefix = config.get('databasePrefix') + 'public'

  if (!remote && req.privilege && !identified.uri.toString().startsWith(publicPrefix)) {
    return privileges.canEdit(req.privilege)
  }

  return false
}

function canShare (identified, req, remote) {
  if (!remote && req.privilege) {
    return privileges.canShare(req.privilege)
  }

  return false
}

module.exports = summarizeIdentified
