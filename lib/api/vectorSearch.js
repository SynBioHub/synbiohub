const request = require('request')

const config = require('../config')

// Query parameters forwarded to SBOLExplorer's /vector_search. As in facets.js,
// the graphs are never taken from the client: they are set below from the
// logged-in user, so nobody can search another user's private parts.
const FORWARDED_PARAMS = ['query', 'size', 'facet_by', 'role', 'sboltype', 'type', 'creator', 'collections']

module.exports = function (req, res) {
  if (!config.get('useSBOLExplorer')) {
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
    url: config.get('SBOLExplorerEndpoint') + 'vector_search',
    qs: qs,
    useQuerystring: true, // repeated keys (graph=a&graph=b), as Flask's getlist expects
    json: true,
    timeout: 30000
  }, (error, response, body) => {
    if (error) {
      console.error('SBOLExplorer /vector_search failed: ' + error)
      return res.status(502).send({ error: 'SBOLExplorer is not responding' })
    }
    // Includes 503 while vector_search is off in SBOLExplorer's config; the frontend shows that.
    res.status(response.statusCode).send(body)
  })
}
