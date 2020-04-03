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

function loginForm (req, res, locals) {
  if (req.user) {
    return res.redirect(req.query.next || '/')
  }

  res.send(pug.renderFile('templates/views/login.jade', {
    config: config.get(),
    nextPage: req.query.next || '/',
    loginAlert: req.flash('login.error').shift() || null,
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
