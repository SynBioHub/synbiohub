
const pug = require('pug')
const db = require('../db')
const config = require('../config')

function clearSessionUser (req) {
  if (req.session && req.session.user !== undefined) {
    delete req.session.user
  }
  req.user = undefined
}

module.exports = function (req, res) {
  const token = req.params.token.trim()

  if (token.length === 0) {
    res.status(400).send('invalid token')
    return
  }

  db.model.User.findOne({

    where: {
      resetPasswordLink: token
    }

  }).then((user) => {
    if (!user) {
      res.status(400).send('bad token')
      return
    }

    clearSessionUser(req)

    var locals = {
      config: config.get(),
      section: 'enterNewPassword',
      nextPage: req.query.next || '/',
      user: null,
      token: token,
      email: user.email,
      requireToken: true
    }

    const renderForm = () => {
      if (!req.accepts('text/html')) {
        return res.status(200).header('content-type', 'text/plain').send('Token Accepted')
      } else {
        res.send(pug.renderFile('templates/views/enterNewPassword.jade', locals))
      }
    }

    if (req.session && typeof req.session.save === 'function') {
      req.session.save(renderForm)
    } else {
      renderForm()
    }
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}
