
var pug = require('pug')

var sparql = require('../../sparql/sparql')

const config = require('../../config')

const CACHE_TTL_MS = 60 * 1000

var graphCountsCache = {
  expires: 0,
  data: null,
  pending: null
}

module.exports = function (req, res) {
  const databasePrefix = config.get('databasePrefix')

  // Prefer explicit JSON over HTML. jQuery's Accept header includes */* at a
  // low q-value, so `req.accepts('text/html')` alone would wrongly match it.
  if (req.accepts(['json', 'html']) === 'json') {
    return getGraphsWithCounts(databasePrefix).then((graphs) => {
      return res.status(200).header('content-type', 'application/json').send(JSON.stringify(graphs))
    }).catch((err) => {
      res.status(500).send(err.stack)
    })
  }

  // HTML: list graphs quickly without per-graph counts so the page can render.
  // Counts are loaded asynchronously via the JSON response above.
  const query = [
    'SELECT DISTINCT ?graph WHERE {',
    'GRAPH ?graph { ?s ?a ?t }',
    '}',
    'ORDER BY ?graph'
  ].join('\n')

  sparql.queryJson(query, null).then((results) => {
    // List only this instance's SBOL data graphs. A triplestore may expose
    // internal graphs of its own (e.g. Virtuoso's system graphs); those are an
    // implementation detail, not SynBioHub data, so keep only graphs under the
    // configured instance prefix. This makes the page identical on any backend.
    const graphs = results
      .map((result) => result.graph)
      .filter((graph) => graph.indexOf(databasePrefix) === 0)
      .map((graphUri) => ({ graphUri: graphUri }))

    var locals = {
      config: config.get(),
      section: 'admin',
      adminSection: 'graphs',
      user: req.user,
      graphs: graphs
    }

    res.send(pug.renderFile('templates/views/admin/graphs.jade', locals))
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}

function getGraphsWithCounts (databasePrefix) {
  if (graphCountsCache.data && Date.now() < graphCountsCache.expires) {
    return Promise.resolve(graphCountsCache.data)
  }

  if (graphCountsCache.pending) {
    return graphCountsCache.pending
  }

  graphCountsCache.pending = fetchGraphsWithCounts(databasePrefix).then((graphs) => {
    graphCountsCache = {
      data: graphs,
      expires: Date.now() + CACHE_TTL_MS,
      pending: null
    }
    return graphs
  }).catch((err) => {
    graphCountsCache.pending = null
    throw err
  })

  return graphCountsCache.pending
}

function fetchGraphsWithCounts (databasePrefix) {
  // One aggregated scan instead of a COUNT(*) round-trip per named graph.
  const countQuery = [
    'SELECT ?graph (COUNT(*) AS ?count) WHERE {',
    'GRAPH ?graph { ?s ?p ?o }',
    '}',
    'GROUP BY ?graph'
  ].join('\n')

  return sparql.queryJson(countQuery, null).then((results) => {
    const graphs = results
      .map((result) => ({
        graphUri: result.graph,
        numTriples: result.count
      }))
      .filter((graph) => graph.graphUri.indexOf(databasePrefix) === 0)

    graphs.sort((a, b) => a.graphUri.localeCompare(b.graphUri))

    return graphs
  })
}
