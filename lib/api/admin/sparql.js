
const sparql = require('../../sparql/sparql')

const SparqlParser = require('sparqljs').Parser
const SparqlGenerator = require('sparqljs').Generator

module.exports = function (req, res) {
  if (req.method === 'POST') {
    query(req, res, req.body.query, req.body['default-graph-uri'])
  } else {
    query(req, res, req.query.query, req.query['default-graph-uri'])
  }
}

function query (req, res, rawQuery, graphUri) {
  graphUri = graphUri || null

  const parser = new SparqlParser()
  const generator = new SparqlGenerator()

  var query
  var queryString

  if (rawQuery) {
    try {
      query = parser.parse(rawQuery)
    } catch (e) {
      res.status(500).send(e.stack)
      return
    }

    queryString = generator.stringify(query)
  }

  sparql.updateQuery(queryString, graphUri, req.header('accept')).then((result) => {
    const { type, statusCode, body } = result

    res.status(statusCode)
    res.header('content-type', type)
    res.send(body)
  }).catch((e) => {
    res.status(500).send(e.stack)
  })
}
