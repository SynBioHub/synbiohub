const loadTemplate = require('../lib/loadTemplate')
const db = require('../lib/db')
const config = require('../lib/config')
const sparql = require('../lib/sparql/sparql')

module.exports = {
  up: (query, DataTypes) => {
    var sparqlQuery = loadTemplate('./sparql/MigrateAttachments.sparql', {})

    var graphs = [config.get('databasePrefix') + 'public']

    return db.model.User.findAll().then(users => {
      users = users.filter(user => !user.virtual)
      users.forEach(user => graphs.push(user.graphUri))

      return Promise.all(graphs.map(graph => sparql.updateQueryJson(sparqlQuery, graph)))
    })
  },

  down: (query, DataTypes) => {
    console.error("You can't unmigrate attachments!")
  }
}
