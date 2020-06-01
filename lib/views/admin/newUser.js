
const pug = require('pug')
const config = require('../../config')
const createUser = require('../../createUser')
const sendCreatePasswordMail = require('../../mail/createPassword')
var extend = require('xtend')
const uuidV4 = require('uuid/v4')
var sha1 = require('sha1')

module.exports = function (req, res) {
  if (config.get('mail').sendgridApiKey !== '') {
    if (req.method === 'POST') {
      post(req, res)
    } else {
      form(req, res, {})
    }
  } else {
    if (!req.accepts('text/html')) {
      return res.status(422).send('SendGrid Email Account Needed')
    } else {
      var locals = {
        config: config.get(),
        section: 'errors',
        user: req.user,
        errors: [ 'SendGrid Email Account Needed' ]
      }
      res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    }
  }
}

function form (req, res, locals) {
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'application/json').send(JSON.stringify(locals.registerAlert))
  } else {
    locals = extend({
      config: config.get(),
      section: 'admin',
      adminSection: 'theme',
      user: req.user,
      form: locals.form || {}
    }, locals)
    res.send(pug.renderFile('templates/views/admin/newUser.jade', locals))
  }
}

function post (req, res) {
  if (!req.body.name) {
    return form(req, res, {
      form: req.body,
      registerAlert: 'Please enter the user\'s name'
    })
  }

  if (!req.body.email) {
    return form(req, res, {
      form: req.body,
      registerAlert: 'Please enter the user\'s email'
    })
  }

  if (!req.body.username) {
    return form(req, res, {
      form: req.body,
      registerAlert: 'Please enter the desired username'
    })
  }

  createUser({
    name: req.body.name,
    email: req.body.email,
    username: req.body.username,
    affiliation: req.body.affiliation,
    password: uuidV4(),
    isAdmin: req.body.isAdmin !== undefined && req.body.isAdmin !== 'false',
    isCurator: req.body.isCurator !== undefined && req.body.isAdmin !== 'false',
    isMember: req.body.isMember !== undefined && req.body.isAdmin !== 'false'
  }).then((user) => {
    user.resetPasswordLink = sha1(uuidV4())

    return user.save()
  }).then((user) => {
    sendCreatePasswordMail(user, req.user)
    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'text/plain').send('New user (' + req.body.username + ') successfully created')
    } else {
      res.redirect('/admin/users/')
    }
  }).catch((err) => {
    console.error(err)
    return form(req, res, {
      form: req.body,
      registerAlert: err
    })
  })
}
