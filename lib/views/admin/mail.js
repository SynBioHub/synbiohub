
const pug = require('pug')
const config = require('../../config')
const extend = require('xtend')
const { resolveMailApiKey, resolveFromAddress } = require('../../mail/mailConfig')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    post(req, res)
  } else {
    form(req, res, {})
  }
}

function currentApiKey () {
  return resolveMailApiKey()
}

function form (req, res, locals) {
  const resendApiKey = currentApiKey()
  const fromAddress = resolveFromAddress()

  locals = extend({
    config: config.get(),
    section: 'admin',
    adminSection: 'mail',
    user: req.user,
    resendApiKey: resendApiKey,
    fromAddress: fromAddress
  }, locals)
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'application/json').send(JSON.stringify({
      resendApiKey: resendApiKey,
      fromAddress: fromAddress
    }))
  } else {
    res.send(pug.renderFile('templates/views/admin/mail.jade', locals))
  }
}

function post (req, res) {
  const submittedKey = (req.body.key || req.body.resendApiKey || '').trim()
  const submittedFrom = (req.body.fromEmail || req.body.fromAddress || '').trim()

  const existingKey = currentApiKey()
  const existingFrom = resolveFromAddress()

  const resendApiKey = submittedKey !== '' ? submittedKey : existingKey
  const fromAddress = submittedFrom !== '' ? submittedFrom : existingFrom

  if (resendApiKey === '' && fromAddress === '') {
    if (!req.accepts('text/html')) {
      return res.status(400).header('content-type', 'text/plain').send('Please enter a Resend API key and/or from address')
    } else {
      return form(req, res, {
        registerAlert: 'Please enter a Resend API key and/or from address'
      })
    }
  }

  config.set('mail', {
    resendApiKey: resendApiKey,
    fromAddress: fromAddress
  })

  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'text/plain').send('Mail configuration successfully updated')
  } else {
    res.redirect('/admin/mail/')
  }
}
