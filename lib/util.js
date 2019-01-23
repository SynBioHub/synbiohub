
var sha1 = require('sha1')
var config = require('./config')

exports.createTriplestoreID = function createTriplestoreID (username) {
  return 'user/' + username
}
