// Migration to change uri column to url in Alias table
module.exports = {
  up: function (query, DataTypes) {
    return query.renameColumn('Alias', 'uri', 'url')
  },
  down: (query, DataTypes) => {
    return query.renameColumn('Alias', 'url', 'uri')
  }
}
