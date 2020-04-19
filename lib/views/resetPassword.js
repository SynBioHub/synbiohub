
var pug = require('pug')
var validator = require('validator')
var extend = require('xtend')
var config = require('../config')
var db = require('../db')

var sha1 = require('sha1')
var uuid = require('uuid')

var sendResetPasswordMail = require('../mail/resetPassword')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    resetPasswordPost(req, res)
  } else {
    resetPasswordForm(req, res, {})
  }
}

function resetPasswordForm (req, res, locals) {
  if (req.user) {
    return res.redirect(req.query.next || '/')
  }

  locals = extend({
    config: config.get(),
    section: 'resetPassword',
    nextPage: req.query.next || '/',
    resetPasswordAlert: null,
    user: req.user
  }, locals)
  if (!req.accepts('text/html')) {
    return res.status(400).header('content-type', 'text/plain').send(locals.resetPasswordAlert)
  } else {
    res.send(pug.renderFile('templates/views/resetPassword.jade', locals))
  }
}

function resetPasswordPost (req, res) {
  if (!req.body.email || !validator.isEmail(req.body.email)) {
    return resetPasswordForm(req, res, {
      form: req.body,
      resetPasswordAlert: 'Please enter a valid e-mail address'
    })
  }

  var locals = {
    config: config.get(),
    section: 'resetPasswordDone',
    user: req.user
  }

  db.model.User.findOne({ where: { email: req.body.email } }).then((user) => {
    user.resetPasswordLink = sha1(uuid.v4())

    return user.save()
  }).then((user) => {
    sendResetPasswordMail(user)
    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'text/plain').send('Password reset successfully')
    } else {
      res.send(pug.renderFile('templates/views/resetPasswordDone.jade', locals))
    }
  }).catch((err) => {
    if (!req.accepts('text/html')) {
      return res.status(500).header('content-type', 'text/plain').send(err)
    } else {
      res.send(pug.renderFile('templates/views/resetPasswordDone.jade', locals))
    }
  })
}
