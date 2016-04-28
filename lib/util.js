
var sha1 = require('sha1');
var config = require('./config')

exports.createTriplestoreID = function createTriplestoreID(email) {
    return 'synbiohub_user_' + sha1('synbiohub_' + sha1(email) + config.get('triplestoreIDSalt'));
}


