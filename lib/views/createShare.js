const access = require('../auth/access')
const config = require('../config')
const db = require('../db')
const getUrisFromReq = require('../getUrisFromReq')
const generateGraph = require('../generateGraph')
const privileges = require('../auth/privileges')
const pug = require('pug')

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

async function post (req, res) {
  const { uri } = getUrisFromReq(req, res)
  let userId = req.body.shareUser
  let privilege = privileges.validate(req.body.privilege)
  let user = await db.model.User.findById(userId)

  let graph = { }
  graph[uri] = generateGraph(uri, req.user)
  access.grant(user, graph, privilege, '')

  res.redirect(req.originalUrl.replace('/createShare', ''))
}
