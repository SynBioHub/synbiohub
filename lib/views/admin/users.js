const pug = require('pug')
<<<<<<< HEAD

=======
>>>>>>> 8279b613... Add virtual users
const db = require('../../db')
const config = require('../../config')

const extend = require('xtend')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    if (req.body.allowPublicSignup) {
      config.set('allowPublicSignup', true)
    } else {
      config.set('allowPublicSignup', false)
    }
  }

  db.model.User.findAll().then((users) => {
    users = users.filter(user => !user.virtual)

    var locals = {
      users: users,
      canSendEmail: config.get('mail').sendgridApiKey !== ''
    }
    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'application/json').send(JSON.stringify(locals))
    } else {
      locals = extend({
        config: config.get(),
        section: 'admin',
        adminSection: 'users',
        user: req.user
      }, locals)

      res.send(pug.renderFile('templates/views/admin/users.jade', locals))
    }
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}
