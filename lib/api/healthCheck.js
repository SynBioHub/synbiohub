const sparql = require('../sparql/sparql')

function healthCheck (req, res) {
  return sparql.query('ASK {}', null, 'text/plain').then(parseResult, handleError)

  function parseResult (result) {
    return res.status(200).type('text/plain').send('Alive')
  }

  function handleError (error) {
    if (error) {
      return res.status(500).type('text/plain').send('Dead')
    }
  }
}

module.exports = healthCheck
