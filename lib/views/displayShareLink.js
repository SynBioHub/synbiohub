const config = require('../config')
const loadTemplate = require('../loadTemplate')
const pug = require('pug')
const privileges = require('../auth/privileges')
const access = require('../auth/access')
const sparql = require('../sparql/sparql')

module.exports = function (req, res) {
  post(req, res)
}

async function generateGraph (uri, user, resolved) {
  let graph = {}
  let prefix = config.get('databasePrefix')
  let top = false

  if (!resolved) {
    resolved = new Set()
    top = true
  }

  resolved.add(uri)

  let parameters = { uri: uri }
  let query = loadTemplate('./sparql/GetChildren.sparql', parameters)
  let children = await sparql.queryJson(query, user.graphUri)

  for (const child of children) {
    let object = child.o

    if (object.startsWith(prefix) && !resolved.has(object)) {
      graph[object] = generateGraph(object, user, resolved)
    }
  }

  if (top) {
    console.log(JSON.stringify(graph))
  }

  return graph
}

function post (req, res) {
  let uri = req.body.uri
  let notes = req.body.shareNotes || ''
  let privilege = privileges.validate(req.body.privileges)

  let graph = { }
  graph[uri] = generateGraph(uri, req.user)

  let shareLink = access.grant(null, graph, privilege, notes)

  let locals = {
    uri: uri,
    notes: notes,
    config: config.get(),
    user: req.user,
    shareLink: shareLink
  }

  res.send(pug.renderFile('templates/views/displayShare.jade', locals))
}
