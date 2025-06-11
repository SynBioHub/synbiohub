const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')
const loadTemplate = require('../loadTemplate')
const sparql = require('../sparql/sparql')

const PREDICATES = {
  'title': 'http://purl.org/dc/terms/title',
  'description': 'http://purl.org/dc/terms/description',
  'wasDerivedFrom': 'http://www.w3.org/ns/prov#wasDerivedFrom',
  'role': 'http://sbols.org/v2#role',
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

function formatObject (field, object) {
  if (field === 'wasDerivedFrom' || field === 'type' || field === 'role' ||
      object.startsWith('http://') || object.startsWith('https://')) {
    return `<${object}>`
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
  if (req.body.pred && req.body.pred !== '') {
    predicate = req.body.pred
  } else {
    predicate = PREDICATES[field]
  }
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
    object: formatObject(field, object.trim()),
    modified: modified
  }

  const query = loadTemplate('./sparql/RemoveTriple.sparql', params)
  console.log(query)

  sparql.updateQueryJson(query, graphUri).then(result => {
    res.sendStatus(200).end()
  }).catch(error => {
    console.error(error)
    res.sendStatus(500).end()
  })
}

module.exports = serve
