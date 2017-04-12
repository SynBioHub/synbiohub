
var pug = require('pug')

var extend = require('xtend')

var config = require('../config')

var validator = require('validator');

var createUser = require('../createUser')

const uuid = require('uuid/v4')

module.exports = function(req, res) {

    if(config.get('firstLaunch') !== true)
        return res.redirect('/')

    var settings = {
        instanceName: 'My SynBioHub',
        instanceUrl: req.protocol + '://' + req.get('Host') + '/',
        userName: '',
        userFullName: '',
        userEmail: '',
        color: '#D25627'
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
        instanceUrl: trim(req.body.instanceURL),
        userName: trim(req.body.userName),
        userFullName: trim(req.body.userFullName),
        userEmail: trim(req.body.userEmail),
        userPassword: req.body.userPassword,
        userPasswordConfirm: req.body.userPasswordConfirm,
    })

    if(settings.instanceName === '') {
        errors.push('Please enter a name for your SynBioHub instance')
    }

    if(settings.userName === '') {
        errors.push('Please enter a username for the initial user account')
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

    config.set('instanceName', settings.instanceName)
    config.set('sessionSecret', uuid())
    config.set('shareLinkSalt', uuid())
    config.set('passwordSalt', uuid())
    config.set('instanceUrl', settings.instanceUrl)
    config.set('databasePrefix', settings.instanceUrl)

    createUser({

        username: settings.userName,
        name: settings.userFullName,
        email: settings.userEmail,
        affiliation: '',
        password: settings.userPassword,
        isAdmin: true

    }).then((user) => {

        config.set('firstLaunch', false)

        req.session.user = user.id

        res.redirect('/');

    }).catch((err) => {

        errors.push('Could not create user')

        errors.push(err.stack)

        return setupForm(req, res, settings, {
            errors: errors
        })

    })
    

}

function trim(input) {

    return input ? input.trim() : ''

}



