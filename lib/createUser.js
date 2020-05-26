const Sequelize = require('sequelize')
const util = require('./util')
const db = require('./db')

const { User } = db.model

async function createUser (info) {
  const graphUri = util.getUserGraphUri(info)
  const [user, created] = await User.findOrCreate({
    where: Sequelize.or(
      { email: info.email },
      { username: info.username }
    ),

    defaults: {
      name: info.name,
      email: info.email,
      username: info.username,
      affiliation: info.affiliation,
      password: User.hashPassword(info.password),
      graphUri: graphUri,
      isAdmin: info.isAdmin !== undefined ? info.isAdmin : false,
      isCurator: info.isCurator !== undefined ? info.isCurator : false,
      isMember: info.isMember !== undefined ? info.isMember : false,
      virtual: info.virtual !== undefined ? info.virtual : false
    }
  })

  if (!created) {
    throw new Error('E-mail address or username already in use')
  }

  return user
}

module.exports = createUser
