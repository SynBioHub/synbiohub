const db = require('../db')

function create (uri, user, tag) {
  db.model.Alias.create({ tag: tag, uri: uri, userId: user.id })
}

function route (req, res, next) {
  // Remove first bit of path
  let tag = req.params.tag
  console.log(`completing tag ${tag}`)

  db.model.Alias.findAll({
    where: { tag: tag }
  }).then(aliases => {
    if (aliases.length !== 1) {
      console.error('Could not complete tag!')
      res.sendStatus(404).end()
    }

    let alias = aliases[0]

    res.redirect(alias.uri)
  })
}

module.exports = {
  route: route,
  create: create
}
