const pug = require('pug')
const db = require('../db')
const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')

module.exports = function (req, res) {
  view(req, res)
}

function view (req, res) {
  const { uri } = getUrisFromReq(req, res)

  db.model.User.findAll().then(users => {
    let locals = {
      config: config.get(),
      user: req.user,
      uri: uri
    }

    res.send(pug.renderFile('templates/views/createShare.jade', locals))
  })
}
