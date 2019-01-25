
const pug = require('pug')
const db = require('../db')

const sha1 = require('sha1')
const config = require('../config')

module.exports = function (req, res) {
  const token = req.body.token.trim()

  if (token.length === 0) {
    res.status(500).send('invalid token')
    return
  }

  db.model.User.findOne({

    where: {
      resetPasswordLink: token
    }

  }).then((user) => {
    if (!user) {
      res.status(500).send('bad token')
      return
    }

    user.resetPasswordLink = ''
    user.password = sha1(config.get('passwordSalt') + sha1(req.body.password1))

    req.session.user = user.id
    req.user = user

    user.save().then(() => {
      res.redirect('/')
    })
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}
