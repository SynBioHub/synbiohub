const loadTemplate = require('../loadTemplate')
const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')
const sparql = require('../sparql/sparql')
const getOwnedBy = require('../query/ownedBy')
const pug = require('pug')

module.exports = function (req, res) {
  req.setTimeout(0) // no timeout

  const { graphUri, uri, edit } = getUrisFromReq(req, res)

  if (!graphUri && !config.get('removePublicEnabled')) {
    return res.status(403).send('Removing membership in public collections is not allowed')
  }

  var memberUri = req.body.member
  if (memberUri.startsWith('/public/')) {
    memberUri = memberUri.replace('/public/', config.get('databasePrefix') + 'public/')
  }
  if (memberUri.startsWith('/user/')) {
    memberUri = memberUri.replace('/user/', config.get('databasePrefix') + 'user/')
  }

  var templateParams = {
    uri: uri,
    memberUri: memberUri
  }

  var removeQuery = loadTemplate('sparql/removeMembership.sparql', templateParams)

  console.log(removeQuery)

  return getOwnedBy(uri, graphUri).then((ownedBy) => {
    let myOwnedBy = config.get('databasePrefix') + 'user/' + req.user.username

    if (!edit && (!req.user || !req.user.username || ownedBy.indexOf(myOwnedBy) === -1)) {
      if (!req.accepts('text/html')) {
        res.status(403).type('text/plain').send('Not authorized to remove a member of this collection')
      } else {
        const locals = {
          config: config.get(),
          section: 'errors',
          user: req.user,
          errors: [ 'Not authorized to remove a member of this collection' ]
        }
        res.status(403).send(pug.renderFile('templates/views/errors/errors.jade', locals))
      }

      return // DO NOT CONTINUE REMOVING
    }

    sparql.updateQuery(removeQuery, graphUri).then(() => {
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
