
const config = require('../config')

const pug = require('pug')

const extend = require('xtend')

const SparqlParser = require('sparqljs').Parser
const SparqlGenerator = require('sparqljs').Generator

const sparql = require('../sparql/sparql')

const checkQuery = require('../checkSparqlQuery')

const getUrisFromReq = require('../getUrisFromReq')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    post(req, res)
  } else {
    form(req, res)
  }
}

function form (req, res, locals) {
  const defaultQuery = []

  const namespaces = config.get('namespaces')

  Object.keys(namespaces).forEach((prefix) => {
    defaultQuery.push('PREFIX ' + prefix.split(':')[1] + ': <' + namespaces[prefix] + '>')
  })

  defaultQuery.push('', '')

  locals = extend({
    config: config.get(),
    section: 'sparql',
    user: req.user,
    errors: [],
    results: '',
    query: defaultQuery.join('\n'),
    graph: 'public',
    url: req.url
  }, locals || {})

  res.send(pug.renderFile('templates/views/sparql.jade', locals))
}

function post (req, res) {
  req.setTimeout(0) // no timeout

  var {
    graphUri,
    baseUri
  } = getUrisFromReq(req, res)

  if (req.url.toString().endsWith('/share/sparql')) {
    // do nothing
  } else if (req.body.graph === 'user') {
    graphUri = req.user.graphUri
  } else {
    graphUri = null
  }

  const parser = new SparqlParser()
  const generator = new SparqlGenerator()

  var query

  try {
    query = parser.parse(req.body.query)
  } catch (e) {
    form(req, res, {
      query: req.body.query,
      graph: req.body.graph,
      errors: [
        e.stack
      ]
    })

    return
  }

  const queryString = generator.stringify(query)

  try {
    checkQuery(query, req.user, baseUri)
  } catch (e) {
    console.log(e.stack)
    form(req, res, {
      query: req.body.query,
      graph: req.body.graph,
      errors: [
        e.stack.split('\n')[0]
      ]
    })

    return
  }

  sparql.queryJson(queryString, graphUri).then((results) => {
    let headers = new Set()

    results.forEach(result => {
      Object.keys(result).forEach(key => {
        headers.add(key)
      })
    })

    form(req, res, {
      query: req.body.query,
      graph: req.body.graph,
      headers: Array.from(headers),
      results: results
    })
  }).catch((e) => {
    form(req, res, {
      query: req.body.query,
      graph: req.body.graph,
      errors: [
        e.stack
      ]
    })
  })
}
