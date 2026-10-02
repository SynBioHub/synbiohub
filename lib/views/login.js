const pug = require('pug')
const db = require('../db')
const config = require('../config')
const apiTokens = require('../apiTokens')
const { dotget } = require('../util')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    loginPost(req, res)
  } else {
    loginForm(req, res, {})
  }
}

function getLoginAlert (req) {
  if (!req.session || !req.session.flash || !req.session.flash['login.error']) {
    return null
  }

  const messages = req.session.flash['login.error']
  const message = messages.shift() || null

  if (messages.length === 0) {
    delete req.session.flash['login.error']

    if (Object.keys(req.session.flash).length === 0) {
      delete req.session.flash
    }
  }

  return message
}

function loginForm (req, res, locals) {
  if (req.user) {
    return res.redirect(req.query.next || '/')
  }

  res.send(pug.renderFile('templates/views/login.jade', {
    config: config.get(),
    nextPage: req.query.next || '/',
    loginAlert: getLoginAlert(req),
    next: req.query.next || '',
    forgotPasswordEnabled: config.get('mail').sendgridApiKey !== '',
    externalAuthProvider: dotget(config.get('externalAuth'), 'provider'),
    ...locals
  }))
}

function showFormError (req, res, message) {
  res.plainOrHtml(
    { status: 401, message },
    () => loginForm(req, res, {
      loginAlert: message,
      next: req.body.next
    })
  )
}

async function loginPost (req, res) {
  if (!req.body.email || !req.body.password) {
    return showFormError(req, res, 'Please enter your e-mail address and password.')
  }

  const user = await db.model.User.findOne({
    where: db.sequelize.or({ email: req.body.email }, { username: req.body.email })
  })

  const passwordHash = db.model.User.hashPassword(req.body.password)

  if (!user) {
    return showFormError(req, res, 'Your e-mail address was not recognized.')
  }

  if (passwordHash !== user.password) {
    return showFormError(req, res, 'Your password was not recognized.')
  }

  res.plainOrHtml(
    { message: apiTokens.createToken(user) },
    () => {
      req.session.user = user.id
      req.session.save(() => {
        res.redirect(req.body.next || '/')
      })
    }
  )
}
