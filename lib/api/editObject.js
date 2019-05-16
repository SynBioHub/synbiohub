const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')
const loadTemplate = require('../loadTemplate')
const sparql = require('../sparql/sparql')

const PREDICATES = {
  'title': 'http://purl.org/dc/terms/title',
  'description': 'http://purl.org/dc/terms/description',
  'role': 'http://sbols.org/v2#role'
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
  if (predicate === PREDICATES['role']) {
    // this needs to actually validate roles
    return true
  }

  return true
}

function serve (req, res) {
  let { graphUri, uri } = getUrisFromReq(req, res)

  let subject = uri
  let predicate = PREDICATES[req.params.field]
  let object = req.body.object || ''

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
    object: `"${object}"` // needs a better way of formatting the object
  }

  const query = loadTemplate('./sparql/UpdateTriple.sparql', params)
  sparql.updateQueryJson(query, graphUri).then(result => {
    res.sendStatus(200).end()
  }).catch(error => {
    console.error(error)
    res.sendStatus(500).end()
  })
}

module.exports = serve
