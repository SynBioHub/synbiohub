const pug = require('pug')
const config = require('../config')
const db = require('../db')
const { dotget } = require('../util')

const { User } = db.model

const extend = require('xtend')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    update(req, res)
  } else {
    display(req, res)
  }
}

function hasPasswordChanged ({ password1, password2 }) {
  if (!password1 || !password2) { return false };
  return (password1 === password2)
}

async function update (req, res) {
  const user = await User.findById(req.user.id)
  const passwordChanged = hasPasswordChanged(req.body)
  const toUpdate = Object.entries({
    name: req.body.name,
    affiliation: req.body.affiliation,
    email: req.body.email,
    password: passwordChanged && User.hashPassword(req.body.password1)
  })

  toUpdate.forEach(([field, value]) => {
    if (value) {
      user[field] = value
    }
  })

  await user.save()
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'text/plain').send('Profile updated successfully')
  } else {
    res.redirect('/profile')
  }
}

function display (req, res) {
  const user = req.user
  const externalProfiles = user.user_external_profiles.map(({ profileName }) => profileName)

  if (!user) {
    res.status(404).send('user not found')
    return
  }

  var locals = {
    user
  }
  if (!req.accepts('text/html')) {
    locals.user.password = ''
    locals.user.resetPasswordLink = ''
    return res.status(200).header('content-type', 'application/json').send(locals.user)
  } else {
    locals = extend({
      config: config.get(),
      section: 'profile',
      externalAuthProvider: dotget(config.get('externalAuth'), 'provider'),
      externalProfiles
    }, locals)
    res.send(pug.renderFile('templates/views/profile.jade', locals))
  }
}
