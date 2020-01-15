const pug = require('pug')
const config = require('../config')
const db = require('../db')
const { dotget } = require('../util')

const { User } = db.model

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

  res.redirect('/profile')
}

function display (req, res) {
  const user = req.user
  const externalProfiles = user.user_external_profiles.map(({ profileName }) => profileName)

  if (!user) {
    res.status(404).send('user not found')
    return
  }

  const locals = {
    config: config.get(),
    section: 'profile',
    user,
    externalAuthProvider: dotget(config.get('externalAuth'), 'provider'),
    externalProfiles
  }

  res.send(pug.renderFile('templates/views/profile.jade', locals))
}
