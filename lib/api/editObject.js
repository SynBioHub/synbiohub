const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')
const loadTemplate = require('../loadTemplate')
const sparql = require('../sparql/sparql')
const lookupRole = require('../role')
const lookupType = require('../type')
const URI = require('urijs')
const getOwnedBy = require('../query/ownedBy')

const PREDICATES = {
  'title': 'http://purl.org/dc/terms/title',
  'description': 'http://purl.org/dc/terms/description',
  'role': 'http://sbols.org/v2#role',
  'wasDerivedFrom': 'http://www.w3.org/ns/prov#wasDerivedFrom',
  'type': 'http://sbols.org/v2#type'
}

function subjectIsValid (subject) {
  let defaultGraph = config.get('triplestore').defaultGraph

  return !subject.startsWith(defaultGraph) || config.get('removePublicEnabled')
}

function predicateIsValid (predicate) {
  return predicate !== undefined
}

function objectIsValid (object) {
  return object && object !== ''
}

function tripleIsValid (subject, predicate, object) {
  if (predicate === PREDICATES['role']) {
    // this needs to actually validate roles
    return true
  }

  return true
}

function getDisplayString (type, object) {
  switch (type) {
    case 'role':
      let role = lookupRole(object)
      return role.description.name || role.term || role.uri
    case 'type':
      let type = lookupType(object)
      return type.description.name || type.term || type.uri
    default:
      return object.trim()
  }
}

function formatObject (field, object) {
  if ((field === 'wasDerivedFrom' || field === 'type' || field === 'role' ||
       object.startsWith('http://') || object.startsWith('https://')) && field !== 'title' && field !== 'description') {
    return `<${URI(object).toString()}>`
  } else {
    return `"${object}"`
  }
}

async function serve (req, res) {
  let { graphUri, uri } = getUrisFromReq(req, res)
  let field = req.params.field

  let ownedBy = await getOwnedBy(uri, graphUri)
  if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
    res.status(401).send('Not authorized to edit this object')
    return
  }

  let d = new Date()
  let modified = d.toISOString()
  modified = modified.substring(0, modified.indexOf('.'))
  modified = JSON.stringify(modified)

  let subject = uri
  var predicate
  if (field === 'annotation') {
    predicate = req.body.pred
  } else {
    predicate = PREDICATES[field]
  }
  let object = req.body.object || ''
  let previous = req.body.previous || ''

  if (!subjectIsValid(subject)) {
    res.sendStatus(403).end()
    return
  } else if (!predicateIsValid(predicate)) {
    res.sendStatus(404).end()
    return
  } else if (!objectIsValid(object)) {
    res.sendStatus(400).end()
    return
  } else if (!tripleIsValid(subject, predicate, object)) {
    res.sendStatus(400).end()
    return
  }

  const params = {
    subject: subject,
    predicate: predicate,
    object: formatObject(field, object),
    previous: (field === 'title' || field === 'description') ? '?previous' : formatObject(field, previous),
    modified: modified
  }

  const query = loadTemplate('./sparql/UpdateTriple.sparql', params)
  console.log('graphUri=' + graphUri)
  console.log('query=' + query)

  sparql.updateQueryJson(query, graphUri).then(result => {
    res.send(getDisplayString(field, object))
  }).catch(error => {
    console.error(error)
    res.sendStatus(500).end()
  })
}

module.exports = serve
