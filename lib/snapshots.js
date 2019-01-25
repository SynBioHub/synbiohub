
const sparql = require('./sparql/sparql')
const loadTemplate = require('./loadTemplate')

function getSnapshots (uri) {
  const query = loadTemplate('./sparql/GetSnapshots.sparql', {
    uri: uri
  })

  return sparql.queryJson(query, null).then((results) => {
    return Promise.resolve(results)
  })
}

module.exports = {
  getSnapshots: getSnapshots
}
