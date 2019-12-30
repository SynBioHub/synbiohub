
const pug = require('pug')

const sparql = require('../sparql/sparql')

const loadTemplate = require('../loadTemplate')

const config = require('../config')

const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

const wiky = require('../wiky/wiky')

const getOwnedBy = require('../query/ownedBy')

module.exports = function (req, res) {
  const uri = req.body.uri

  const graphUri = getGraphUriFromTopLevelUri(uri, req.user)

  const source = req.body.value

  var sourceSparql = ''
  if (source.trim() !== '') {
    sourceSparql = '<' + uri + '> sbh:mutableProvenance ' + JSON.stringify(source) + ' .'
  }

  var d = new Date()
  var modified = d.toISOString()
  modified = modified.substring(0, modified.indexOf('.'))

  const updateQuery = loadTemplate('./sparql/UpdateMutableSource.sparql', {
    topLevel: uri,
    source: sourceSparql,
    modified: JSON.stringify(modified)
  })

  getOwnedBy(uri, graphUri).then((ownedBy) => {
    if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
      res.status(401).send('not authorized to edit this submission')
      return
    }

    return sparql.updateQuery(updateQuery, graphUri).then((result) => {
      if (!req.accepts('text/html')) {
        return res.status(200).send('Success')
      } else {
        const locals = {
          config: config.get(),
          src: source,
          source: source !== '' ? wiky.process(source, {}) : '',
          canEdit: true
        }

        res.send(pug.renderFile('templates/partials/mutable-source.jade', locals))
      }
    })
  })
}
