const retrieveUris = require('../retrieveUris')
const config = require('../config')
const sparql = require('../sparql/sparql')
const getUrisFromReq = require('../getUrisFromReq')
const loadTemplate = require('../loadTemplate')
const getOwnedBy = require('../query/ownedBy')

module.exports = function (req, res) {
  const { graphUri, uri, url } = getUrisFromReq(req, res)

  return getOwnedBy(uri, graphUri).then((ownedBy) => {
    if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
      return res.status(401).send('Not authorized to remove an owner')
    }

    return retrieveUris(uri, graphUri).then((uris) => {
      let chunks = []
      let offset = config.get('resolveBatch')

      for (let i = 0; i < uris.length; i += offset) {
        let end = i + offset < uris.length ? i + offset : uris.length
        chunks.push(uris.slice(i, end))
      }

      const sharedRemovalQuery = loadTemplate('./sparql/RemoveFromSharedCollection.sparql', {
        uri: uri,
        userUri: req.body.userUri
      })

      console.log(sharedRemovalQuery)

      return sparql.updateQuery(sharedRemovalQuery, req.body.userUri)
        .then(() => {
          return Promise.all(
            chunks.map((chunk) => {
              let uris = chunk
                .map((uri) => `<${uri}> sbh:ownedBy <${req.body.userUri}>`)
                .join(' . \n')

              const updateQuery = loadTemplate('./sparql/RemoveOwnedBy.sparql', { uris })
              console.log(updateQuery)

              return sparql.updateQuery(updateQuery, graphUri)
            })
          )
        })
        .then(() => {
          // Redirect only once after all promises resolve
          if (!req.accepts('text/html')) {
            res.status(200).send('Success')
          } else {
            res.redirect(url)
          }
        })
        .catch((err) => {
          console.error('Error:', err)
          res.status(500).send(`Error removing ownership: ${err.message}`)
        })
    })
  })
}
