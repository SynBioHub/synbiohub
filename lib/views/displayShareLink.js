const config = require('../config')
const pug = require('pug')
const privileges = require('../auth/privileges')
const access = require('../auth/access')
const generateGraph = require('../generateGraph')

module.exports = function (req, res) {
  post(req, res)
}

function post (req, res) {
  let uri = req.body.uri
  let url = req.body.url
  let notes = req.body.shareNotes || ''
  let privilege = privileges.validate(req.body.privileges)

  let graph = { }
  graph[uri] = generateGraph(uri, req.user)

  let shareLink = access.grant(null, graph, privilege, notes)

  let locals = {
    url: url,
    notes: notes,
    config: config.get(),
    user: req.user,
    shareLink: shareLink
  }

  res.send(pug.renderFile('templates/views/displayShare.jade', locals))
}
