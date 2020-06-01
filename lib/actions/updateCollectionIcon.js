
const fs = require('fs')
const config = require('../config')
const extend = require('xtend')

const getOwnedBy = require('../query/ownedBy')

var getUrisFromReq = require('../getUrisFromReq')

const pug = require('pug')

const mkdirp = require('mkdirp')

module.exports = function (req, res) {
  const { graphUri, uri } = getUrisFromReq(req, res)

  return getOwnedBy(uri, graphUri).then((ownedBy) => {
    if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
      if (!req.accepts('text/html')) {
        res.status(403).type('text/plain').send('Not authorized to update this collection icon')
      } else {
        const locals = {
          config: config.get(),
          section: 'errors',
          user: req.user,
          errors: [ 'Not authorized to update this collection icon' ]
        }
        res.status(403).send(pug.renderFile('templates/views/errors/errors.jade', locals))
      }
    }

    if (req.file) {
      var iconFile = req.file

      var collectionIcons = config.get('collectionIcons')
      const iconFilename = 'public/icons/' + iconFile.originalname

      mkdirp('public/icons').then(() => {
        fs.writeFileSync(iconFilename, iconFile.buffer)
      })

      collectionIcons = extend(collectionIcons, {
        [req.body.collectionUri]: '/icons/' + iconFile.originalname
      })

      config.set('collectionIcons', collectionIcons)
    }
    if (!req.accepts('text/html')) {
      res.status(200).type('text/plain').send('Success')
    } else {
      res.redirect(req.body.collectionUrl)
    }
  })
}
