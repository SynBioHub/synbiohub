
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
    return sendResetPasswordMail(user)
  }).then(() => {
    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'text/plain').send('If a user was found associated with the specified e-mail address, a link to reset your password has been sent.')
    } else {
      res.send(pug.renderFile('templates/views/resetPasswordDone.jade', locals))
    }
  }).catch((err) => {
    if (err === {}) {
      if (!req.accepts('text/html')) {
        return res.status(200).header('content-type', 'text/plain').send('If a user was found associated with the specified e-mail address, a link to reset your password has been sent.')
      } else {
        res.send(pug.renderFile('templates/views/resetPasswordDone.jade', locals))
      }
    } else {
      var error = err
      var statusCode = 500
      if (err.message) {
        error = err.message
        if (err.response) {
          if (err.response.statusCode) {
            statusCode = err.response.statusCode
          }
          if (err.response.body) {
            error = err.response.body
            if (err.response.body.errors) {
              error = ''
              err.response.body.errors.forEach((errorMessage) => {
                error += errorMessage.message + ' '
              })
            }
          }
        }
      }
      console.error(statusCode + ': ' + JSON.stringify(error))
      if (!req.accepts('text/html')) {
        return res.status(statusCode).header('content-type', 'text/plain').send(error)
      } else {
        locals.resetPasswordAlert = error
        res.send(pug.renderFile('templates/views/resetPasswordError.jade', locals))
      }
    }
  })
}
