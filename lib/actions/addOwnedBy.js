const sparql = require('../sparql/sparql')
const loadTemplate = require('../loadTemplate')
const config = require('../config')
const getOwnedBy = require('../query/ownedBy')
const retrieveUris = require('../retrieveUris')
const getUrisFromReq = require('../getUrisFromReq')
const db = require('../db')

module.exports = async function (req, res) {
  const userUri = req.body.user

  var userId = ''
  if (req.body.user.lastIndexOf('/') >= 0) {
    userId = req.body.user.substring(req.body.user.lastIndexOf('/') + 1)
  }

  const user = await db.model.User.findOne({
    where: db.sequelize.or({ email: userId }, { username: userId })
  })

  const { graphUri, uri } = getUrisFromReq(req, res)

  const sharedAdditionQuery = loadTemplate('./sparql/AddToSharedCollection.sparql', {
    uri: uri,
    userUri: userUri
  })

  return getOwnedBy(uri, graphUri).then((ownedBy) => {
    if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
      // res.status(401).send('not authorized to edit this submission')
      return new Promise(function (resolve, reject) {
        reject(new Error('not authorized to add an owner'))
      })
    }

    if (!user) {
      return new Promise(function (resolve, reject) {
        reject(new Error('user ' + userId + ' not recognized'))
      })
    }

    return sparql.updateQuery(sharedAdditionQuery, userUri).then(() => {
      return retrieveUris(uri, graphUri)
    }).then((uris) => {
      let chunks = []
      let offset = config.get('resolveBatch')

      for (let i = 0; i < uris.length; i += offset) {
        let end = i + offset < uris.length ? i + offset : uris.length

        chunks.push(uris.slice(i, end))
      }

      return Promise.all(chunks.map(chunk => {
        let uris = chunk.map(uri => {
          return '<' + uri + '> sbh:ownedBy <' + userUri + '>'
        }).join(' . \n')

        const updateQuery = loadTemplate('./sparql/AddOwnedBy.sparql', {
          uris: uris
        })

        return sparql.updateQuery(updateQuery, graphUri)
      }))
    })
  })
}
