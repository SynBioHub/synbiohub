const virtualUser = require('./virtualUser')
const config = require('../config')
const uuid = require('uuid/v4')
const db = require('../db')

/** grant(user, uris)
 * user: either a User object (from lib/db) or undefined
 *       -> if User, grant user access to all URIs
 *       -> if undefined, create virtual user and grant
 *          access to all URIs
 * uriGraph: graph of URIs to authorize for user as follows
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
function grant (user, uriGraph) {
  if (Object.keys(uriGraph).length !== 1) {
    throw new Error('There should only be one root URI!')
  }

  let shareTag = uuid()
  let accessionUrl = config.get('instanceUrl') + shareTag

  // This promise is not returned so that the function returns
  // quickly, and the database accesses are done asynchronously
  validateUser(user)
    .then(validatedUser => createAuthorizations(validatedUser, uriGraph))

  return accessionUrl
}

function revoke (user, uris) {}

function view (user, uri) {}

// If user is undefined, create virtual user
// If user is defined, make sure they exist
function validateUser (user) {
  if (!user) {
    return virtualUser.create()
  }

  return Promise.resolve(user)
}

function createAuthorizations (user, uriGraph, root) {
  let uris = Object.keys(uriGraph)

  uris.forEach(uri => {
    console.log(`Authorizing ${user.username} to access ${uri}`)
    db.model.Auth.create({
      uri: uri,
      userId: user.id,
      root: root
    })

    createAuthorizations(user, uriGraph[uri], uri)
  })
}

module.export = {
  grant: grant,
  revoke: revoke,
  view: view
}
