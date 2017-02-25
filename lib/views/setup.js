
var pug = require('pug')

var extend = require('xtend')

var config = require('../config')

var validator = require('validator');

var createUser = require('../createUser')

module.exports = function(req, res) {

    if(config.get('firstLaunch') !== true)
        return res.redirect('/')

    var settings = {
        instanceName: 'My SynBioHub',
        stackURL: 'http://localhost:9090',
        userName: '',
        userEmail: '',
        stackUsername: '',
        stackPassword: ''
    }

    if(req.method === 'POST') {

        setupPost(req, res, settings)

    } else {

        setupForm(req, res, settings, {})

    }
};


function setupForm(req, res, settings, locals) {

	locals = extend({
        config: config.get(),
        title: 'First Time Setup - SynBioHub',
        settings: settings,
        errors: []
    }, locals)
	
    res.send(pug.renderFile('templates/views/setup.jade', locals))

}

function setupPost(req, res, settings) {

    var errors = []

    settings = extend(settings, {
        instanceName: trim(req.body.instanceName),
        userName: trim(req.body.userName),
        userEmail: trim(req.body.userEmail),
        userPassword: req.body.userPassword,
        userPasswordConfirm: req.body.userPasswordConfirm,
        stackURL: trim(req.body.stackURL),
        stackUsername: trim(req.body.stackUsername),
        stackPassword: req.body.stackPassword,
    })

    if(settings.instanceName === '') {
        errors.push('Please enter a name for your Hub')
    }

    if(settings.userName === '') {
        errors.push('Please enter a name for the initial user account')
    }

    if(!settings.userEmail || !validator.isEmail(settings.userEmail)) {
        errors.push('Please enter a valid e-mail address for the initial user account')
    }

    if(settings.userPassword === '') {

        errors.push('Please enter a password for the initial user account')

    } else {

        if(settings.userPassword !== settings.userPasswordConfirm) {
            errors.push('Passwords did not match')
        }

    }

    if(errors.length > 0) {

        return setupForm(req, res, settings, {
            errors: errors
        })
    }

    config.set('backendURL', settings.stackURL)
    config.set('backendUser', settings.stackUsername)
    config.set('backendPassword', settings.stackPassword)

    createUser({

        name: settings.userName,
        email: settings.userEmail,
        affiliation: '',
        password: settings.userPassword

    }, (err, user) => {

        if(err) {

            errors.push('Could not create user in stack ' + stack.backendUrl)

            errors.push(err.stack)

            return setupForm(req, res, settings, {
                errors: errors
            })

        } else {

            config.set('firstLaunch', false)

            req.session.user = user.id

            res.redirect('/');
        }

    })
    

}

function trim(input) {

    return input ? input.trim() : ''

}



