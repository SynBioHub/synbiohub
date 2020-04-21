
var pug = require('pug')

var sparql = require('../../sparql/sparql')

const config = require('../../config')

module.exports = function (req, res) {
  const query = [
    'SELECT DISTINCT ?graph WHERE {',
    'GRAPH ?graph { ?s ?a ?t }',
    '}'
  ].join('\n')

  sparql.queryJson(query, null).then((results) => {
    const graphs = results.map((result) => result.graph)

    return Promise.all(
      graphs.map((graph) => graphInfo(graph))
    )
  }).then((graphs) => {
    var locals = {
      config: config.get(),
      section: 'admin',
      adminSection: 'graphs',
      user: req.user,
      graphs: graphs
    }
    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'application/json').send(JSON.stringify(graphs))
    } else {
      res.send(pug.renderFile('templates/views/admin/graphs.jade', locals))
    }
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
