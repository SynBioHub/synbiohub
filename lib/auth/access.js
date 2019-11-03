const virtualUser = require('./virtualUser')
const alias = require('./alias')
const config = require('../config')
const uuid = require('uuid/v4')
const db = require('../db')

/** grant(user, uris)
 * user: either a User object (from lib/db) or undefined
 *       -> if User, grant user access to all URIs
 *       -> if undefined, create virtual user and grant
 *          access to all URIs
 * uriGraph: Promise that resolves to a graph of URIs to
 *           authorize for user as follows
 *
 * {
 *   topLevelUri: {
 *     secondLevelOne: {
 *       thirdLevel... // continues...
 *     },
 *     secondLevelTwo: {} // this is a leaf
 * }
 *
 * RETURNS: the URL where topLevelUri can be accessed
 */
function grant (user, uriGraph, privilege, notes) {
  if (Object.keys(uriGraph).length !== 1) {
    throw new Error('There should only be one root URI!')
  }

  let instanceUrl = config.get('instanceUrl')
  let databasePrefix = config.get('databasePrefix')

  let shareTag = uuid()
  let root = Object.keys(uriGraph)[0].replace(databasePrefix, instanceUrl)
  let accessionUrl = instanceUrl + 'alias/' + shareTag

  // This promise is not returned so that the function returns
  // quickly, and the database accesses are done asynchronously
  validateUser(user)
    .then(async validatedUser => {
      if (validatedUser.virtual) {
        alias.create(root, validatedUser, shareTag, notes)
      }

      createAuthorizations(validatedUser, uriGraph, privilege)
    })

  return accessionUrl
}

async function getPrivilege (userIds, path) {
  let uri = config.get('databasePrefix') + path.substring(1)
  let username = path.substring(6, path.indexOf('/', 6))

  let user = await db.model.User.findAll({
    where: {
      id: userIds,
      username: username
    }
  })

  if (user.length > 0) {
    // belongs to a logged inuser
    return 3
  }

  let auths = await db.model.Auth.findAll({
    where: {
      userId: userIds,
      uri: uri
    }
  })

  let privileges = auths.map(auth => parseInt(auth.privilege) || 0)
  let maxPrivilege = Math.max(0, ...privileges)

  return maxPrivilege
}

// If user is undefined, create virtual user
// If user is defined, make sure they exist
function validateUser (user) {
  if (!user) {
    return virtualUser.create()
  }

  return Promise.resolve(user)
}

async function createAuthorizations (user, uriGraph, privilege, root) {
  let uris = Object.keys(uriGraph)

  uris.forEach(async uri => {
    let current = await db.model.Auth.create({
      uri: uri,
      userId: user.id,
      rootAuth: root,
      privilege: privilege
    })

    let subgraph = await uriGraph[uri]
    createAuthorizations(user, subgraph, privilege, current.id)
  })
}

async function getShared (users) {
  let userIds = users.map(user => user.id)

  let auths = await db.model.Auth.findAll({
    where: {
      userId: userIds,
      rootAuth: null
    }
  })

  let topLevels = auths.map(auth => {
    let databasePrefix = config.get('databasePrefix')
    let instanceUrl = config.get('instanceUrl')

    return {
      uri: auth.uri,
      url: auth.uri.replace(databasePrefix, instanceUrl)
    }
  })

  return topLevels
}

module.exports = {
  grant: grant,
  getPrivilege: getPrivilege,
  getShared: getShared
}
