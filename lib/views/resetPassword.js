
var pug = require('pug')
var User = require('../models/User')
var validator = require('validator');
var util = require('../util');
var async = require('async')
var extend = require('xtend')
var config = require('../config')

var sha1 = require('sha1')

module.exports = function(req, res) {
    
    if(req.method === 'POST') {

        registerPost(req, res)

    } else {

        registerForm(req, res, {})

    }
    
}

function registerForm(req, res, locals) {

	if (req.user) {

		return res.redirect(req.query.next || '/');

	}

    locals = extend({
        section: 'register',
        nextPage: req.query.next || '/',
        registerAlert: null,
        user: req.user
    }, locals)
    
    res.send(pug.renderFile('templates/views/resetPassword.jade', locals))
}


function registerPost(req, res) {

    if(!req.body.name) {
        return registerForm(req, res, {
            form: req.body,
            registerAlert: 'Please enter your name'
        })
    }

    if(!req.body.email || !validator.isEmail(req.body.email)) {
        return registerForm(req, res, {
            form: req.body,
            registerAlert: 'Please enter a valid e-mail address'
        })
    }

    if(!req.body.password1) {
        return registerForm(req, res, {
            form: req.body,
            registerAlert: 'Please enter a password'
        })
    }

    if(req.body.password1 !== req.body.password2) {
        return registerForm(req, res, {
            form: req.body,
            registerAlert: 'Passwords do not match'
        })
    }

    var user

    async.series([

        function checkEmail(next) {

            User.findOne({ email: req.body.email }, function(err, _user) {

                if(err || !_user) {

                    registerForm(req, res, {
                        form: req.body,
                        registerAlert: 'No user with that e-mail address'
                    })

                } else {

		    user = _user
                    next()

                }
            })
        },

        function updateUser(next) {

            user.name = req.body.name
            user.affiliation = req.body.affiliation || '',
            user.password = sha1(config.get('passwordSalt') + sha1(req.body.password1)),
	    user.save((err) => {

                if(err) {

                    registerForm(req, res, {
                        form: req.body,
                        registerAlert: err.stack
                    })

                } else {

                    req.session.user = user.id

                    res.redirect(req.query.next || '/');

                }
            })
        }
    ])
    
}

