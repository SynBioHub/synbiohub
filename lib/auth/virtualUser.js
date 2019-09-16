const db = require('../db')

async function create () {
  return db.model.User.create({ virtual: true })
}

async function remove (id) {
  let user = await db.model.User.findById(id)

  if (!user.virtual) {
    throw new Error('Cannot erase non-virtual user.')
  }

  db.model.Auth.destroy({
    where: {
      userId: user.id
    }
  })

  user.destroy()
}

module.exports = {
  create: create,
  remove: remove
}
