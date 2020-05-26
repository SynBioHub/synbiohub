// Migration to add authorization table
const db = require('../lib/db')

module.exports = {
  up: (query, DataTypes) => {
    // If auth table exists, exit
    // Otherwise, create the table
    return query.describeTable(db.model.Alias.tableName)
      .catch(() => db.model.Alias.sync())
  },
  down: (query, DataTypes) => {
    // If auth table does not exist, exit
    // Otherwise, drop the auth table
    return query.describeTable(db.model.Alias.tableName)
      .catch() // If we got an error, then there was no table
      .then(() => db.model.Alias.drop())
  }
}
