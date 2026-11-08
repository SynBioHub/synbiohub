const request = require('request')

const config = require('../config')

// Query parameters forwarded to SBOLExplorer's /facets. The graphs are never
// taken from the client: they are set below from the logged-in user, the same
// way lib/search.js scopes searches, so nobody can count another user's
// private parts.
const FORWARDED_PARAMS = ['query', 'facet_by', 'size', 'role', 'sboltype', 'type', 'creator', 'collections']

module.exports = function (req, res) {
  if (!config.get('useSBOLExplorer')) {
    // The frontend falls back to computing facets with SPARQL.
    return res.status(501).send({ error: 'SBOLExplorer is not enabled' })
  }

  const qs = {}
  FORWARDED_PARAMS.forEach((name) => {
    if (req.query[name] !== undefined) {
      qs[name] = req.query[name]
    }
  })
  qs.graph = [config.get('databasePrefix') + 'public']
  if (req.user && req.user.graphUri) {
    qs.graph.push(req.user.graphUri)
  }

  request({
    method: 'GET',
    url: config.get('SBOLExplorerEndpoint') + 'facets',
    qs: qs,
    useQuerystring: true, // repeated keys (graph=a&graph=b), as Flask's getlist expects
    json: true,
    timeout: 30000
  }, (error, response, body) => {
    if (error) {
      console.error('SBOLExplorer /facets failed: ' + error)
      return res.status(502).send({ error: 'SBOLExplorer is not responding' })
    }
    // Includes 404 from an SBOLExplorer without /facets; the frontend falls back.
    res.status(response.statusCode).send(body)
  })
}
