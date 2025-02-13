
const {v4: uuidv4 } = require('uuid')
const db = require('./db')

const tokens = Object.create(null)

function createToken (user) {
  const token = uuidv4()

  tokens[token] = user.id

  return token
}
function deleteToken (token) {
  delete tokens[token]
}

function getUserFromToken (token) {
  const uid = tokens[token]

  if (uid === undefined) { return null }

  return db.model.User.findById(uid)
}

function getUserIdFromToken (token) {
  return tokens[token] || null
}

module.exports = {
  createToken,
  deleteToken,
  getUserFromToken,
  getUserIdFromToken
}
