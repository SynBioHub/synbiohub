
const fs = require('fs')
const process = require('process')
const path = require('path')
const config = require('../../config')

module.exports = function (logoFile) {
  let splitFilename = logoFile.originalname.split('.')
  let last = splitFilename.length - 1
  let extension = splitFilename[last]

  const logoFilename = path.resolve(process.cwd(), 'public/logo_uploaded.' + extension)

  fs.writeFileSync(logoFilename, logoFile.buffer)

  config.set('instanceLogo', '/logo_uploaded.' + extension)
  config.set('logoFilename', logoFilename)
}
