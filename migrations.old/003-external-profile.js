const db = require('../lib/db')

const { UserExternalProfile } = db.model

module.exports = {
  up: (query) => {
    return query.describeTable(UserExternalProfile.tableName)
      .catch(() => UserExternalProfile.sync())
  },

  down: (query) => {
    return query.describeTable(UserExternalProfile.tableName)
      .catch()
      .then(() => UserExternalProfile.drop())
  }
}
