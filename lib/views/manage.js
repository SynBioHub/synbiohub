
var pug = require('pug')

var search = require('../search')

var config = require('../config')

function compareSubmissions (a, b) {
  const aName = (a.name || a.displayId || '').toLowerCase()
  const bName = (b.name || b.displayId || '').toLowerCase()
  if (aName < bName) return -1
  if (aName > bName) return 1
  // Names can coincide across collections and versions; identity breaks ties.
  if (a.uri < b.uri) return -1
  if (a.uri > b.uri) return 1
  return 0
}

module.exports = function (req, res) {
  var locals = {
    config: config.get(),
    section: 'manage',
    privateSubmissions: [],
    publicSubmissions: [],
    user: req.user
  }

  /*
var criteria = [
'?collection a sbol2:Collection .',
'?collection synbiohub:uploadedBy "' + req.user.email + '" .',
'?collection sbol2:member ?subject .'
].join('\n') */

  var userCriteria = '{ ?subject synbiohub:uploadedBy "' + req.user.email + '" } UNION { ?subject sbh:ownedBy <' + config.get('databasePrefix') + 'user/' + req.user.username + '> } .'

  var criteria = [
    '?subject a sbol2:Collection . ' + userCriteria +
'FILTER NOT EXISTS { ?otherCollection sbol2:member ?subject }'
  ].join('\n')

  return Promise.all([
    search(null, criteria, undefined, undefined),
    search(req.user.graphUri, criteria, undefined, undefined)
  ]).then(([publicSearch, privateSearch]) => {
    // Wait for both searches before filtering: either graph can finish first.
    const publicURIs = new Set(publicSearch.results.map(result => result.uri))
    locals.publicSubmissions = publicSearch.results.map(result => {
      return Object.assign({}, result, { triplestore: 'public' })
    }).sort(compareSubmissions)
    locals.privateSubmissions = privateSearch.results.filter(result => {
      return !publicURIs.has(result.uri)
    }).map(result => {
      return Object.assign({}, result, { triplestore: 'private' })
    }).sort(compareSubmissions)

    // SPARQL result order is unspecified; both HTML and JSON use the same order.
    if (!req.accepts('text/html')) {
      var result = locals.privateSubmissions.concat(locals.publicSubmissions)
      return res.status(200).header('content-type', 'application/json').send(JSON.stringify(result))
    } else {
      locals.removePublicEnabled = config.get('removePublicEnabled')
      res.send(pug.renderFile('templates/views/manage.jade', locals))
    }
  })
}
