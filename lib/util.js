const config = require('./config')

const createTriplestoreID = (username) => `user/${username}`
const getUserGraphUri = ({ username }) => `${config.get('databasePrefix')}${createTriplestoreID(username)}`

module.exports = {
  createTriplestoreID,
  getUserGraphUri
}
