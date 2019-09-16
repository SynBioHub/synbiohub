const db = require('../db')

module.exports = async function (req, res) {
  let toRemove = [ req.params.id ]

  while (toRemove.length > 0) {
    let id = toRemove.shift()
    let children = await db.model.Auth.findAll({
      where: {
        rootAuth: id
      }
    })

    children.forEach(child => {
      toRemove.push(child.id)
    })

    db.model.Auth.destroy({
      where: {
        id: id
      }
    })
  }

  res.redirect(req.header('Referer'))
}
