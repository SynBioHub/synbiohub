
var sha1 = require('sha1')

var util = require('./util');

var config = require('./config')

var db = require('./db')

function createUser(info) {

    var graphUri = 
        config.get('databasePrefix') + util.createTriplestoreID(info.email)


    console.log('creating user')
    console.log(JSON.stringify(info))

    return db.model.User.findOrCreate({

        where: {
            email: info.email,
        },

        defaults: {
            name: info.name,
            email: info.email,
            affiliation: info.affiliation,
            password: sha1(config.get('passwordSalt') + sha1(info.password)),
            graphUri: graphUri,
            isAdmin: false
        }
    
    }).then((res) => {

        const user = res[0]
        const created = res[1]

        if(!created) {

            return Promise.reject(new Error('E-mail address already in use'))

        } else {

            return Promise.resolve(user)

        }

    })


}

module.exports = createUser


