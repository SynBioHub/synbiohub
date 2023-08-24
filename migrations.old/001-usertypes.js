// var Bluebird = require('bluebird')

module.exports = {
  up: (query, DataTypes) => {
    var isMember = false
    var isCurator = false

    return query.sequelize.query('PRAGMA table_info(user)', { type: DataTypes.QueryTypes.SELECT })
      .then(columns => {
        columns.forEach(function (column) {
          if (column.name === 'isMember') { isMember = true }

          if (column.name === 'isCurator') { isCurator = true }
        }, this)

        var queries = []

        if (!isMember) {
          queries.push([query.addColumn('user', 'isMember', {
            type: DataTypes.BOOLEAN,
            allowNull: false,
            defaultValue: false
          }),
          query.sequelize.query(`UPDATE "user" SET "isMember" = "isAdmin"`, { raw: true })
          ])
        }

        if (!isCurator) {
          queries.push([query.addColumn('user', 'isCurator', {
            type: DataTypes.BOOLEAN,
            allowNull: false,
            defaultValue: false
          }),
          query.sequelize.query(`UPDATE "user" SET "isCurator" = "isAdmin"`, { raw: true })
          ])
        }

        return Promise.all(queries)
      })
  },

  down: (query, DataTypes) => {
    var isMember = false
    var isCurator = false

    return query.sequelize.query('PRAGMA table_info(user)', { type: DataTypes.QueryTypes.SELECT })
      .then(columns => {
        columns.forEach(function (column) {
          if (column.name === 'isMember') { isMember = true }

          if (column.name === 'isCurator') {
            isCurator = true
          }
        }, this)

        var queries = []

        if (isMember) {
          queries.push(query.removeColumn('user', 'isMember'))
        }

        if (isCurator) {
          queries.push(query.removeColumn('user', 'isCurator'))
        }

        return Promise.all(queries)
      })
  }
}
