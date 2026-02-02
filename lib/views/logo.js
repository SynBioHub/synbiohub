const config = require('../config')
const path = require('path')
const fs = require('fs')

function get (req, res) {
  // Prefer logoFilename (full absolute path) if available
  let logoFilename = config.get('logoFilename')

  if (logoFilename && fs.existsSync(logoFilename)) {
    // Use the absolute path directly
    return res.sendFile(logoFilename)
  }

  // Fall back to instanceLogo (relative path)
  let instanceLogo = config.get('instanceLogo')
  if (!instanceLogo) {
    return res.status(404).send('Logo not found')
  }

  // Remove leading slash if present, as res.sendFile with root option expects relative path
  let relativePath = instanceLogo.startsWith('/') ? instanceLogo.substring(1) : instanceLogo
  var dir = path.resolve(__dirname, '../../uploads')
  let fullPath = path.join(dir, relativePath)

  // Check if file exists
  if (!fs.existsSync(fullPath)) {
    return res.status(404).send('Logo not found')
  }

  // Send the file using root option
  res.sendFile(relativePath, { root: dir })
}

module.exports = get
