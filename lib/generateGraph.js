const config = require('./config')
const loadTemplate = require('./loadTemplate')
const sparql = require('./sparql/sparql')

async function generateGraph (uri, user, resolved) {
  let graph = {}
  let prefix = config.get('databasePrefix')

  if (!resolved) {
    resolved = new Set()
  }

  resolved.add(uri)

  let parameters = { uri: uri }
  let query = loadTemplate('./sparql/GetChildren.sparql', parameters)
  let children = await sparql.queryJson(query, user.graphUri)

  for (const child of children) {
    let object = child.o
    let path = object.replace(prefix, '')

    if (!object.startsWith(prefix)) {
      continue
    }

    if (resolved.has(object)) {
      continue
    }

    if (path.startsWith('public')) {
      continue
    }

    if (path.split('/').length === 2) { // It's just a user
      continue
    }

    if (path.match(/\/[0-9]+$/) === null) { // No version
      continue
    }

    graph[object] = generateGraph(object, user, resolved)
  }

  return graph
}

module.exports = generateGraph
