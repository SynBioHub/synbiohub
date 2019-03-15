const pug = require('pug')
const db = require('../db')
const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')
const addOwnedBy = require('../actions/addOwnedBy')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    post(req, res)
  } else {
    view(req, res)
  }
}

function view (req, res) {
  const { uri } = getUrisFromReq(req, res)

  db.model.User.findAll().then(users => {
    users = users.filter(user => user !== req.user.username && !user.virtual)

    let locals = {
      config: config.get(),
      user: req.user,
      users: users,
      uri: uri
    }

    res.send(pug.renderFile('templates/views/addOwner.jade', locals))
  })
}

function post (req, res) {
  addOwnedBy(req, res).then(() => {
    res.redirect(req.originalUrl.replace('/addOwner', ''))
  }, function (err) {
    const locals = {
      config: config.get(),
      section: 'errors',
      user: req.user,
      errors: [ err ]
    }
    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
  })
}
