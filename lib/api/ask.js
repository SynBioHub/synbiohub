const request = require('request')

const config = require('../config')

// Forwards a question to SBOLExplorer's /ask, which answers it with Claude
// using GraphRAG retrieval and read-only SPARQL as tools.
//
// Only the public graph is passed, even for a logged-in user: the tools'
// results are sent to Anthropic's API, so private parts stay out unless that
// is decided explicitly. Login is required because every question is a billed
// API call.
module.exports = function (req, res) {
  if (!config.get('useSBOLExplorer')) {
    return res.status(501).send({ error: 'SBOLExplorer is not enabled' })
  }

  const question = req.body && typeof req.body.question === 'string' ? req.body.question.trim() : ''
  if (!question) {
    return res.status(400).send({ error: 'question is required' })
  }

  request({
    method: 'POST',
    url: config.get('SBOLExplorerEndpoint') + 'ask',
    body: {
      question: question,
      graph: [config.get('databasePrefix') + 'public']
    },
    json: true,
    timeout: 180000 // several model turns and tool calls
  }, (error, response, body) => {
    if (error) {
      console.error('SBOLExplorer /ask failed: ' + error)
      return res.status(502).send({ error: 'SBOLExplorer is not responding' })
    }
    // Includes 503 while ask is off in SBOLExplorer's config; the frontend shows that.
    res.status(response.statusCode).send(body)
  })
}
