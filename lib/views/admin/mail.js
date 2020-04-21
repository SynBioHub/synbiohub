
const pug = require('pug')
const config = require('../../config')
const extend = require('xtend')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    post(req, res)
  } else {
    form(req, res, {})
  }
}

function form (req, res, locals) {
  var mail = config.get('mail')

  locals = extend({
    config: config.get(),
    section: 'admin',
    adminSection: 'mail',
    user: req.user,
    sendGridApiKey: mail.sendgridApiKey,
    sendGridFromEmail: mail.fromAddress
  }, locals)
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'application/json').send(JSON.stringify({
      sendGridApiKey: mail.sendgridApiKey,
      sendGridFromEmail: mail.fromAddress
    }))
  } else {
    res.send(pug.renderFile('templates/views/admin/mail.jade', locals))
  }
}

function post (req, res) {
  if (!req.body.key) {
    if (!req.accepts('text/html')) {
      return res.status(400).header('content-type', 'text/plain').send('Please enter the SendGrid API Key')
    } else {
      return form(req, res, {
        registerAlert: 'Please enter the SendGrid API Key'
      })
    }
  }

  if (!req.body.fromEmail) {
    if (!req.accepts('text/html')) {
      return res.status(400).header('content-type', 'text/plain').send('Please enter the SendGrid From email')
    } else {
      return form(req, res, {
        registerAlert: 'Please enter the SendGrid From email'
      })
    }
  }

  var mail = {}
  mail.sendgridApiKey = req.body.key
  mail.fromAddress = req.body.fromEmail
  config.set('mail', mail)
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'text/plain').send('Mail configuration successfully updated')
  } else {
    res.redirect('/admin/mail/')
  }
}
