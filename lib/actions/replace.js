
var request = require('request')

var loadTemplate = require('../loadTemplate')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

var sparql = require('../sparql/sparql')

const getOwnedBy = require('../query/ownedBy')

const pug = require('pug')

module.exports = function (req, res) {
  req.setTimeout(0) // no timeout

  const { graphUri, uri, edit } = getUrisFromReq(req, res)

  var templateParams = {
    uri: uri
  }

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

  var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

  return getOwnedBy(uri, graphUri).then((ownedBy) => {
    if (!edit && (!req.user || !req.user.username || ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1)) {
      if (!req.accepts('text/html')) {
        res.status(403).type('text/plain').send('Not authorized to replace this object')
        return
      } else {
        const locals = {
          config: config.get(),
          section: 'errors',
          user: req.user,
          errors: [ 'Not authorized to replace this object' ]
        }
        res.status(403).send(pug.renderFile('templates/views/errors/errors.jade', locals))
      }
    }

    sparql.deleteStaggered(removeQuery, graphUri).then(() => {
      if (!req.accepts('text/html')) {
        res.status(200).type('text/plain').send('Success')
      } else {
        res.redirect('/manage')
      }
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
