const pug = require('pug')
const db = require('../db')
const config = require('../config')
const apiTokens = require('../apiTokens')

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
    section: 'login',
    nextPage: req.query.next || '/',
    loginAlert: null,
    user: req.user,
    next: req.query.next || '',
    forgotPasswordEnabled: config.get('mail').sendgridApiKey !== '',
    ...locals
  }))
}

async function loginPost (req, res) {
  if (!req.body.email || !req.body.password) {
    return res.plainOrHtml(
      { status: 401, message: 'Please enter your e-mail address and password.' },
      () => loginForm(req, res, {
        form: req.body,
        loginAlert: 'Please enter your e-mail address and password.'
      })
    )
  }

  const user = await db.model.User.findOne({
    where: db.sequelize.or({ email: req.body.email }, { username: req.body.email })
  })

  const passwordHash = db.model.User.hashPassword(req.body.password)

  if (!user) {
    return res.plainOrHtml(
      { status: 401, message: 'Your e-mail address was not recognized.' },
      () => loginForm(req, res, {
        form: req.body,
        loginAlert: 'Your e-mail address was not recognized.',
        next: req.body.next
      })
    )
  }

  if (passwordHash !== user.password) {
    return res.plainOrHtml(
      { status: 401, message: 'Your password was not recognized.' },
      () => loginForm(req, res, {
        form: req.body,
        loginAlert: 'Your password was not recognized.',
        next: req.body.next
      })
    )
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
