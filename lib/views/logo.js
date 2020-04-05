const config = require('../config')
const path = require('path')

function get (req, res) {
  var dir = path.resolve(__dirname, '../../uploads')
  let logoFilename = config.get('instanceLogo')
  res.sendFile(logoFilename, { root: dir })
}

module.exports = get
