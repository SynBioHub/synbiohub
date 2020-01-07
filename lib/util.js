const config = require('./config')

const createTriplestoreID = (username) => `user/${username}`
const getUserGraphUri = ({ username }) => `${config.get('databasePrefix')}${createTriplestoreID(username)}`

/**
 * Retrieve nested value from object using dot notation
 *
 * @param {Object} object
 * @param {String} path
 * @return {*}
 */
const dotget = (object, path) => {
  const keys = path.split('.')

  return keys.reduce(
    (value, currentKey) => (value && value.hasOwnProperty(currentKey))
      ? value[currentKey]
      : undefined,
    object
  )
}

module.exports = {
  createTriplestoreID,
  getUserGraphUri,
  dotget
}
