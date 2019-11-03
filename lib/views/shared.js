const access = require('../auth/access')
const pug = require('pug')
const config = require('../config')
const loadTemplate = require('../loadTemplate')
const sparql = require('../sparql/sparql')
const sha1 = require('sha1')

async function getSharedByOwnedBy (user) {
  let databasePrefix = config.get('databasePrefix')
  let userUri = databasePrefix + 'user/' + user.username
  let values = {
    userUri: userUri
  }

  const sharedCollectionQuery = loadTemplate('./sparql/GetSharedCollection.sparql', values)

  let collectionUris = await sparql.queryJson(sharedCollectionQuery, user.graphUri)
  console.log(collectionUris)

  let collectionData = collectionUris.map(collectionUri => {
    let shareHash = sha1('synbiohub_' + sha1(collectionUri) + config.get('shareLinkSalt'))
    let shareUrl = '/' + collectionUri.toString().replace(databasePrefix, '') + '/' + shareHash + '/share'

    return {
      uri: collectionUri,
      url: shareUrl
    }
  })

  return collectionData
}

function getGraphUri (uri) {
  uri = uri.toString()

  let publicGraph = config.get('triplestore').defaultGraph
  if (uri.startsWith(publicGraph)) {
    return publicGraph
  }

  let userPosition = uri.indexOf('/user/')
  let end = uri.indexOf('/', userPosition + 6)
  return uri.substring(0, end)
}

async function getCollectionMetadata (uri) {
  let graphUri = getGraphUri(uri)

  let query = loadTemplate('./sparql/getCollectionMetaData.sparql', { collection: uri })
  return sparql.queryJson(query, graphUri)
}

module.exports = async function (req, res) {
  let sharedWithOwnedBy = await getSharedByOwnedBy(req.user.username)
  let shared = await access.getShared(req.users)
  let collated = sharedWithOwnedBy.concat(shared)
  let defaultGraph = config.get('triplestore').defaultGraph

  let sharedMetadata = await Promise.all(collated.map(async topLevel => {
    let metadata = await getCollectionMetadata(topLevel.uri)
    metadata = metadata[0]
    metadata.url = topLevel.url
    if (topLevel.uri.toString().startsWith(defaultGraph)) {
      metadata.triplestore = 'public'
    } else {
      metadata.triplestore = 'private'
    }

    return metadata
  }))

  let locals = {
    config: config.get(),
    section: 'shared',
    user: req.user,
    searchResults: sharedMetadata
  }

  res.send(pug.renderFile('templates/views/shared.jade', locals))
}
