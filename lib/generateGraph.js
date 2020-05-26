const config = require('./config')
const loadTemplate = require('./loadTemplate')
const sparql = require('./sparql/sparql')

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

module.exports = generateGraph
