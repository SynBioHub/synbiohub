
const fs = require('fs')
const config = require('../../config')

module.exports = function(logoFile) {
    const logoFilename = 'logo_uploaded.' + logoFile.originalname.split('.')[1]

    fs.writeFileSync('public/' + logoFilename, logoFile.buffer)

    config.set('instanceLogo', '/' + logoFilename)
}

