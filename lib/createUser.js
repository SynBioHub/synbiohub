
var User = require('./models/User')

var sha1 = require('sha1')

var util = require('./util');

var config = require('./config')

function createUser(info, callback) {

    var stack = require('./stack')()

    var triplestoreName = util.createTriplestoreID(info.email)

    stack.createStore(triplestoreName, function(err, store) {

        if(err) {
            return callback(err)
        }

        var user = new User({
            name: info.name,
            email: info.email,
            affiliation: info.affiliation,
            password: sha1(config.get('passwordSalt') + sha1(info.password)),
            storeName: triplestoreName
        });

        user.save(function(err) {

            if(err) {
                return callback(err)
            }

            callback(null, user)
        })

    })

}

module.exports = createUser


