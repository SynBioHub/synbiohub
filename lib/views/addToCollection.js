const loadTemplate = require('../loadTemplate')
const pug = require('pug')
const config = require('../config')
const sparql = require('../sparql/sparql')
const getOwnedBy = require('../query/ownedBy')

module.exports = function (req, res) {
  req.setTimeout(0) // no timeout

  function addToCollectionsForm (req, res) {
    var collectionQuery = loadTemplate('sparql/getCollections.sparql', {})

    function sortByNames (a, b) {
      if (a.name < b.name) {
        return -1
      } else {
        return 1
      }
    }

    return sparql.queryJson(collectionQuery, req.user.graphUri).then((collections) => {
      collections.map((result) => {
        result.uri = result.subject
        result.name = result.name ? result.name : result.uri.toString()
        delete result.subject
      })
      collections.sort(sortByNames)

      const locals = {
        config: config.get(),
        section: 'addToCollection',
        user: req.user,
        collections: collections,
        addToCollectionUrl: req.url,
        errors: {}
      }
      res.send(pug.renderFile('templates/views/addToCollection.jade', locals))
    })
  }

  if (req.method === 'POST') {
    const uri = req.body.collections.toString() // .replace(config.get('databasePrefix'),config.get('instanceUrl'))
    const returnUrl = req.url.toString().replace('/addToCollection', '')
    var memberUri = returnUrl.replace(config.get('instanceUrl'), config.get('databasePrefix'))

    if (!memberUri) {
      if (!req.accepts('text/html')) {
        return res.status(400).type('text/plain').send('Must provide URI of member to add')
      } else {
        const locals = {
          config: config.get(),
          section: 'errors',
          user: req.user,
          errors: [ 'Must provide URI of member to add' ]
        }
        return res.status(400).send(pug.renderFile('templates/views/errors/errors.jade', locals))
      }
    }

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

    var addQuery = loadTemplate('sparql/addMembership.sparql', templateParams)

    console.log(addQuery)

    return getOwnedBy(uri, req.user.graphUri).then((ownedBy) => {
      let myOwnedBy = config.get('databasePrefix') + 'user/' + req.user.username

      if (!req.user || !req.user.username || ownedBy.indexOf(myOwnedBy) === -1) {
        if (!req.accepts('text/html')) {
          return res.status(403).type('text/plain').send('Not authorized to add to this collection')
        } else {
          const locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ 'Not authorized to add to this collection' ]
          }
          return res.status(403).send(pug.renderFile('templates/views/errors/errors.jade', locals))
        }

        // DO NOT CONTINUE ADDING
      }

      sparql.updateQuery(addQuery, req.user.graphUri).then(() => {
        if (!req.accepts('text/html')) {
          res.status(200).type('text/plain').send('Success')
        } else {
          res.redirect(returnUrl)
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
  } else {
    return addToCollectionsForm(req, res)
  }
}
