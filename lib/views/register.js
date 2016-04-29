
var pug = require('pug')
var User = require('../models/User')
var stack = require('../stack')()
var validator = require('validator');
var util = require('../util');
var async = require('async')
var extend = require('xtend')
var config = require('../config')

var createUser = require('../createUser')

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
    
    res.send(pug.renderFile('templates/views/register.jade', locals))
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

    // todo move verification into createUser?
    //
    async.series([

        function checkEmail(next) {

            User.findOne({ email: req.body.email }, function(err, user) {

                if(err || user) {

                    registerForm(req, res, {
                        form: req.body,
                        registerAlert: 'E-mail address already in use'
                    })

                } else {

                    next()

                }
            })
        },

        function addUser(next) {

            createUser({

                name: req.body.name,
                email: req.body.email,
                affiliation: req.body.affiliation || '',
                password: req.body.password1

            }, (err, user) => {

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

