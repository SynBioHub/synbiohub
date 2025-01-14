
const config = require('../../config')

const pug = require('pug')

const extend = require('xtend')

const SparqlParser = require('sparqljs').Parser
const SparqlGenerator = require('sparqljs').Generator

const sparql = require('../../sparql/sparql')

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

  const query = [
    'SELECT DISTINCT ?graph WHERE {',
    'GRAPH ?graph { ?s ?a ?t }',
    '}'
  ].join('\n')

  sparql.queryJson(query, null).then((results) => {
    var graphs = results.map((result) => result.graph)

    graphs = graphs.filter(result => result.startsWith(config.get('triplestore').graphPrefix))

    // Alphabetize the filtered graphs
    graphs.sort((a, b) => a.localeCompare(b))

    return Promise.all(
      graphs.map((graph) => graphInfo(graph))
    )
  }).then((graphs) => {
    locals = extend({
      config: config.get(),
      section: 'admin',
      adminSection: 'sparql',
      user: req.user,
      errors: [],
      results: '',
      query: defaultQuery.join('\n'),
      graphs: graphs
    }, locals || {})

    res.send(pug.renderFile('templates/views/admin/sparql.jade', locals))
  }).catch((err) => {
    res.status(500).send(err.stack)
  })

  function graphInfo (graphUri) {
    const countTriplesQuery = [
      'SELECT (COUNT(*) as ?count) WHERE {',
      '?s ?p ?o .',
      '}'
    ].join('\n')

    return sparql.queryJson(countTriplesQuery, graphUri).then((results) => {
      return Promise.resolve({
        graphUri: graphUri,
        numTriples: results[0].count
      })
    })
  }
}

function post (req, res) {
  req.setTimeout(0) // no timeout

  var graphUri = req.body.graph

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

  console.debug(queryString)

  sparql.updateQueryJson(queryString, graphUri).then((results) => {
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
