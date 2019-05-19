const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')
const loadTemplate = require('../loadTemplate')
const sparql = require('../sparql/sparql')

const PREDICATES = {
  'title': 'http://purl.org/dc/terms/title',
  'description': 'http://purl.org/dc/terms/description'
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

function formatObject (object) {
  if (object.startsWith('http')) {
    return `<${object}>`
  } else {
    return `"${object}"`
  }
}

function serve (req, res) {
  let { graphUri, uri } = getUrisFromReq(req, res)

  let d = new Date()
  let modified = d.toISOString()
  modified = modified.substring(0, modified.indexOf('.'))
  modified = JSON.stringify(modified)

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
  }

  const params = {
    subject: subject,
    predicate: predicate,
    object: formatObject(object),
    modified: modified
  }

  const query = loadTemplate('./sparql/UpdateTriple.sparql', params)
  console.log(query)

  sparql.updateQueryJson(query, graphUri).then(result => {
    res.sendStatus(200).end()
  }).catch(error => {
    console.error(error)
    res.sendStatus(500).end()
  })
}

module.exports = serve
