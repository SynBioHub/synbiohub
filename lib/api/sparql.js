
const sparql = require('../sparql/sparql')

const SparqlParser = require('sparqljs').Parser
const SparqlGenerator = require('sparqljs').Generator

const checkQuery = require('../checkSparqlQuery')

const getUrisFromReq = require('../getUrisFromReq')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    query(req, res, req.body.query, req.body['default-graph-uri'])
  } else {
    query(req, res, req.query.query, req.query['default-graph-uri'])
  }
}

function query (req, res, rawQuery, graphUri) {
  const parser = new SparqlParser()
  const generator = new SparqlGenerator()

  var query
  var queryString

  if (rawQuery) {
    try {
      query = parser.parse(rawQuery)
    } catch (e) {
      res.status(400).send(e.stack)
      return
    }

    queryString = generator.stringify(query)

    var baseUri

    if (req.url.toString().indexOf('?') >= 0 &&
        req.url.toString().substring(0, req.url.toString().indexOf('?')).endsWith('/share/sparql')) {
      ({ graphUri,
        baseUri
      } = getUrisFromReq(req, res))
    } else {
      graphUri = graphUri || null
    }

    try {
      checkQuery(query, req.user, baseUri)
    } catch (e) {
      res.status(400).send(e.stack.split('\n')[0])
      return
    }
  }

  sparql.query(queryString, graphUri, req.header('accept')).then((result) => {
    const { type, statusCode, body } = result

    res.status(statusCode)
    res.header('content-type', type)
    res.send(body)
  }).catch((e) => {
    res.status(500).send(e.stack)
  })
}
