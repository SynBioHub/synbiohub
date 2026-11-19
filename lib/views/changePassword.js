const pug = require('pug')
const config = require('../config')

module.exports = function (req, res) {
  if (!req.user) {
    if (!req.accepts('text/html')) {
      return res.status(401).send('Login required')
    }
    return res.redirect('/login?next=' + encodeURIComponent(req.url))
  }

  const locals = {
    config: config.get(),
    section: 'enterNewPassword',
    user: req.user,
    token: '',
    email: req.user.email,
    requireToken: false,
    formAction: '/setNewPassword',
    registerAlert: null
  }

  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'application/json').send({
      email: req.user.email
    })
  }

  res.send(pug.renderFile('templates/views/enterNewPassword.jade', locals))
}
