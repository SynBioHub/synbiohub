
var sha1 = require('sha1')

var util = require('./util');

var config = require('./config')

var db = require('./db')

function createUser(info, callback) {

    var triplestoreName = util.createTriplestoreID(info.email)

    db.model.User.create({
        name: info.name,
        email: info.email,
        affiliation: info.affiliation,
        password: sha1(config.get('passwordSalt') + sha1(info.password)),
        storeName: triplestoreName

    }).then((user) => {

        callback(null, user)
    })


}

module.exports = createUser


