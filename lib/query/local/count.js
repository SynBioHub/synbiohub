
const loadTemplate = require('../../loadTemplate')
const sparql = require('../../sparql/sparql')

function getCount (type, graphUri) {
  var query = loadTemplate('./sparql/Count.sparql', {
    type: type
  })

  return sparql.queryJson(query, graphUri).then((result) => {
    if (result && result[0]) {
      return Promise.resolve(parseInt(result[0].count))
    } else {
      return Promise.reject('not found')
    }
  })
}

module.exports = {
  getCount: getCount
}
