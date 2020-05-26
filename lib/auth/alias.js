const db = require('../db')

function create (url, user, tag, description) {
  db.model.Alias.create({
    tag: tag,
    url: url,
    userId: user.id,
    description: description
  })
}

async function remove (tag) {
  let alias = await db.model.Alias.findOne({
    where: {
      tag: tag
    }
  })

  let user = alias.userId
  alias.destroy()

  return user
}

function route (req, res, next) {
  // Remove first bit of path
  let tag = req.params.tag

  db.model.Alias.findAll({
    where: { tag: tag }
  }).then(aliases => {
    if (aliases.length !== 1) {
      console.error('Could not complete tag!')
      res.sendStatus(404).end()
    }

    let alias = aliases[0]
    let users = req.session.users || []
    users.push(alias.userId)
    req.session.users = users
    console.log(JSON.stringify(req.session.users))

    res.redirect(alias.url)
  })
}

module.exports = {
  route: route,
  create: create,
  remove: remove
}
