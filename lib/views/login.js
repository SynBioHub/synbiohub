
var pug = require('pug')
var extend = require('xtend')

var db = require('../db')

var sha1 = require('sha1')

var config = require('../config')

module.exports = function(req, res) {

    if(req.method === 'POST') {

        loginPost(req, res)

    } else {

        loginForm(req, res, {})

    }
}

function loginForm(req, res, locals) {
	
	if (req.user) {

		return res.redirect(req.query.next || '/');

	}

    locals = extend({
        section: 'login',
        nextPage: req.query.next || '/',
        loginAlert: null,
        user: req.user,
        next: req.query.next || ''
    }, locals)
    
    res.send(pug.renderFile('templates/views/login.jade', locals))
}

function loginPost(req, res) {

    if (!req.body.email || !req.body.password) {

        return loginForm(req, res, {
            form: req.body,
            loginAlert: 'Please enter your e-mail address and password.'
        })

    }

    db.model.User.findOne({

        where: {
            email: req.body.email
        }

    }).then((user) => {

        var passwordHash = sha1(config.get('passwordSalt') + sha1(req.body.password))

        if(!user) {
            return loginForm(req, res, {
                form: req.body,
                loginAlert: 'Your e-mail address was not recognized.',
                next: req.body.next
            })

        }

        console.log('rbp is ' + req.body.password)
        console.log('hash is ' + passwordHash)
        console.log('user pw is ' + user.password)

        if(passwordHash !== user.password) {
            return loginForm(req, res, {
                form: req.body,
                loginAlert: 'Your password was not recognized.',
                next: req.body.next
            })

        } 

        req.session.user = user.id

		res.redirect(req.body.next || '/');
    })


		

}

