const request = require('request')
const loadTemplate = require('../loadTemplate')
const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')
const sparql = require('../sparql/sparql')
const getOwnedBy = require('../query/ownedBy')
const pug = require('pug')

module.exports = function (req, res) {
  req.setTimeout(0) // no timeout

  const { graphUri, uri, edit } = getUrisFromReq(req, res)

  var templateParams = {
    uri: uri
  }

  var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

  return getOwnedBy(uri, graphUri).then((ownedBy) => {
    let myOwnedBy = config.get('databasePrefix') + 'user/' + req.user.username

    if (!edit && (!req.user || !req.user.username || ownedBy.indexOf(myOwnedBy) === -1)) {
      if (!req.accepts('text/html')) {
        res.status(403).type('text/plain').send('Not authorized to remove this object')
      } else {
        const locals = {
          config: config.get(),
          section: 'errors',
          user: req.user,
          errors: [ 'Not authorized to remove this object' ]
        }
        res.status(403).send(pug.renderFile('templates/views/errors/errors.jade', locals))
      }

      return // DO NOT CONTINUE REMOVING
    }

    sparql.deleteStaggered(removeQuery, graphUri).then(() => {
      var templateParams = {
        uri: uri
      }

      removeQuery = loadTemplate('sparql/removeReferences.sparql', templateParams)

      sparql.deleteStaggered(removeQuery, graphUri).then(() => {
        if (config.get('useSBOLExplorer')) {
          request({
            method: 'GET',
            url: config.get('SBOLExplorerEndpoint') + 'incrementalremove',
            qs: { subject: uri }
          }, function (error, response, body) {
            console.error(error)
            console.error(body)
          })
        }
        if (!req.accepts('text/html')) {
          res.status(200).type('text/plain').send('Success')
        } else {
          res.redirect('/manage')
        }
      })
    }).catch((err) => {
      console.error(err.stack)
      if (!req.accepts('text/html')) {
        res.status(500).send(err.stack)
      } else {
        const locals = {
          config: config.get(),
          section: 'errors',
          user: req.user,
          errors: [ err.stack ]
        }
        res.status(500).send(pug.renderFile('templates/views/errors/errors.jade', locals))
      }
    })
  })
}
