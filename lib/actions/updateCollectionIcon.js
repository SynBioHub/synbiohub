
const fs = require('fs')
const config = require('../config')
const extend = require('xtend')

module.exports = function (req, res) {
    console.log("Entered change function")
    if (req.file) {
        iconFile = req.file

        var collectionIcons = config.get('collectionIcons')
        const iconFilename = 'public/local/' + iconFile.originalname

        fs.writeFileSync(iconFilename, iconFile.buffer)

        collectionIcons = extend(collectionIcons, {
            [req.body.collectionUri]: '/local/' + iconFile.originalname
        })

        config.set('collectionIcons', collectionIcons)
    }

    res.redirect(req.body.collectionUrl)
}

