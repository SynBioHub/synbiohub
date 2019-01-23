const config = require('../config')

function get (req, res) {
  let logoFilename = config.get('logoFilename')
  res.sendFile(logoFilename)
}

module.exports = get
