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

  let shareTag = uuid()
  let root = Object.keys(uriGraph)[0]
  let accessionUrl = config.get('instanceUrl') + 'alias/' + shareTag

  // This promise is not returned so that the function returns
  // quickly, and the database accesses are done asynchronously
  validateUser(user)
    .then(async validatedUser => {
      alias.create(root, validatedUser, shareTag)
      createAuthorizations(validatedUser, uriGraph, privilege, notes)
    })

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

async function createAuthorizations (user, uriGraph, privilege, notes, root) {
  let uris = Object.keys(uriGraph)
  console.log(uriGraph)

  console.log(`Authorizing for ${user.id}`)

  uris.forEach(async uri => {
    console.log(`Granting access to ${uri}`)
    db.model.Auth.create({
      uri: uri,
      userId: user.id,
      root: root
    })

    let subgraph = await uriGraph[uri]
    console.log(subgraph)
    createAuthorizations(user, subgraph, uri)
  })
}

module.exports = {
  grant: grant,
  revoke: revoke,
  view: view
}
