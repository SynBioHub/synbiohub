
var pug = require('pug')
var extend = require('xtend')

var User = require('../models/User')

var sha1 = require('sha1')

var config = require('../config')

module.exports = function(req, res) {

    remoteLoginPost(req, res)

}

function remoteLoginPost(req, res) {

    console.log(req.body)

    if (!req.body.email || !req.body.password) {
	console.log('no email or password')
	res.status(500).type('text/plain').send('Please enter your e-mail address and password.')
	return
    }

    User.findOne({

        email: req.body.email

    }, (err, user) => {

        var passwordHash = sha1(config.get('passwordSalt') + sha1(req.body.password))

	console.log(req.body.email);
	console.log(req.body.password);
        if(!user) {
	    console.log('user not found')
	    res.status(500).type('text/plain').send('Your e-mail address was not recognized.')
	    return
        }

        if(passwordHash !== user.password) {
	    console.log('password not found')
	    res.status(500).type('text/plain').send('Your password was not recognized.')
	    return

        } 

        //req.session.user = user.id
	console.log(user)
	res.status(200).type('text/plain').send(user)

    })


		

}

