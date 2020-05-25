/* var request = require('request') */

var loadTemplate = require('../loadTemplate')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

var sparql = require('../sparql/sparql')

const getOwnedBy = require('../query/ownedBy')

const pug = require('pug')

function sendExplorerIncrementalRemoveCollectionRequest (uri, uriPrefix) {
  return new Promise((resolve, reject) => {
    /*    if (config.get('useSBOLExplorer')) {
      request({
        method: 'GET',
        url: config.get('SBOLExplorerEndpoint') + 'incrementalremovecollection',
        qs: { subject: uri, uriPrefix: uriPrefix }
      }, function (error, response, body) {
        if (error) {
          console.log(error)
        }
        resolve()
      })
    } else { */
    resolve()
    /* } */
  })
}

module.exports = function (req, res) {
  req.setTimeout(0) // no timeout

  const { graphUri, uri } = getUrisFromReq(req, res)

  if (!graphUri && !config.get('removePublicEnabled')) {
    return res.status(403).send('Removing public submissions is not allowed')
  }

  var uriPrefix = uri.substring(0, uri.lastIndexOf('/'))
  uriPrefix = uriPrefix.substring(0, uriPrefix.lastIndexOf('/') + 1)

  var templateParams = {
    collection: uri,
    uriPrefix: uriPrefix,
    version: req.params.version
  }

  return sendExplorerIncrementalRemoveCollectionRequest(uri, uriPrefix).then(() => {
    var removeQuery = loadTemplate('sparql/removeCollection.sparql', templateParams)

    return getOwnedBy(uri, graphUri).then((ownedBy) => {
      if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
        if (!req.accepts('text/html')) {
          res.status(403).type('text/plain').send('Not authorized to remove this submission')
        } else {
          const locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ 'Not authorized to remove this submission' ]
          }
          res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
        }
      }

      sparql.deleteStaggered(removeQuery, graphUri).then(() => {
        templateParams = {
          uri: uri
        }
        removeQuery = loadTemplate('sparql/remove.sparql', templateParams)
        sparql.deleteStaggered(removeQuery, graphUri).then(() => {
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
  })
}
