
const fs = require('fs')
const config = require('../../config')

module.exports = function(req, res) {

    if(!req.file) {
        res.status(500).send('no file?')
        return
    }

    const logoFilename = 'logo_uploaded.' + req.file.originalname.split('.')[1]

    fs.writeFileSync('public/' + logoFilename, req.file.buffer)

    config.set('instanceLogo', '/' + logoFilename)

    res.redirect('/admin/theme')
}

