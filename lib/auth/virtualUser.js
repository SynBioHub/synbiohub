const db = require('../db')

function create () {
  return db.model.User.create({ virtual: true })
}

module.exports = {
  create: create
}
