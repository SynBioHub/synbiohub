
var pug = require('pug')
var extend = require('xtend')

var db = require('../db')

var sha1 = require('sha1')

var config = require('../config')

module.exports = function(req, res) {

    remoteLoginPost(req, res)

}

function remoteLoginPost(req, res) {

    if (!req.body.email || !req.body.password) {
	res.status(500).type('text/plain').send('Please enter your e-mail address and password.')
	return
    }

    db.model.User.findOne({

        email: req.body.email

    }).then((user) => {

        var passwordHash = sha1(config.get('passwordSalt') + sha1(req.body.password))

        if(!user) {
	    res.status(500).type('text/plain').send('Your e-mail address was not recognized.')
	    return
        }

        if(passwordHash !== user.password) {
	    res.status(500).type('text/plain').send('Your password was not recognized.')
	    return

        } 

        //req.session.user = user.id
	res.status(200).type('text/plain').send(user)

    })


		

}

