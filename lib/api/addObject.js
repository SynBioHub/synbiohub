const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')
const loadTemplate = require('../loadTemplate')
const sparql = require('../sparql/sparql')
const lookupRole = require('../role')
const lookupType = require('../type')
const URI = require('urijs')

const PREDICATES = {
  'wasDerivedFrom': 'http://www.w3.org/ns/prov#wasDerivedFrom',
  'role': 'http://sbols.org/v2#role',
  'type': 'http://sbols.org/v2#type'
}

function subjectIsValid (subject) {
  let defaultGraph = config.get('triplestore').defaultGraph

  return !subject.startsWith(defaultGraph)
}

function predicateIsValid (predicate) {
  return predicate !== undefined
}

function objectIsValid (object) {
  return object && object !== ''
}

function tripleIsValid (subject, predicate, object) {
  if (PREDICATES[predicate] && predicate === PREDICATES[predicate]) {
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
      return object
  }
}

function formatObject (field, object) {
  if (field === 'wasDerivedFrom' || field === 'type' || field === 'role' ||
      object.startsWith('http://') || object.startsWith('https://')) {
    return `<${URI(object).toString()}>`
  } else {
    return `"${object}"`
  }
}

function serve (req, res) {
  let { graphUri, uri } = getUrisFromReq(req, res)
  let field = req.params.field

  let d = new Date()
  let modified = d.toISOString()
  modified = modified.substring(0, modified.indexOf('.'))
  modified = JSON.stringify(modified)

  let subject = uri
  var predicate
  var object
  console.log('field=' + field)
  console.log('pred=' + req.body.pred)
  console.log('obj=' + req.body.object)
  if (field === 'annotation') {
    if (!req.body.pred || req.body.pred === '' || !req.body.object || req.body.object === '') {
      res.sendStatus(403).end()
      return
    }
    predicate = req.body.pred
    object = req.body.object
  } else {
    predicate = PREDICATES[field]
    object = req.body.object || ''
  }
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
    modified: modified
  }

  const query = loadTemplate('./sparql/AddTriple.sparql', params)
  console.log(query)

  sparql.updateQueryJson(query, graphUri).then(result => {
    res.send(getDisplayString(field, object))
  }).catch(error => {
    console.error(error)
    res.sendStatus(500).end()
  })
}

module.exports = serve
