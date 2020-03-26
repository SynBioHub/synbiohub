
const pug = require('pug')
const db = require('../db')
const config = require('../config')

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

    req.session.user = user.id
    req.user = user

    var locals = {
      config: config.get(),
      section: 'enterNewPassword',
      nextPage: req.query.next || '/',
      user: req.user,
      token: token
    }

    res.send(pug.renderFile('templates/views/enterNewPassword.jade', locals))
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}
