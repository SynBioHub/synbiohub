const db = require('../lib/db')

module.exports = {
  up: (query, DataTypes) => {
    return query.describeTable(db.model.User.tableName)
      .then(table => {
        if (table.virtual) {
          return
        }

        return query.addColumn('user', 'virtual', {
          type: DataTypes.BOOLEAN,
          allowNull: false,
          defaultValue: false
        })
      })
  },
  down: (query, DataTypes) => {
    return query.describeTable(db.model.User.tableName)
      .then(table => {
        if (!table.virtual) {
          return
        }

        return query.removeColumn('user', 'virtual')
      })
  }
}
