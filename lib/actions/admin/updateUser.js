
const db = require('../../db')

module.exports = function (req, res) {
  var error = false
  db.model.User.findById(req.body.id).then((user) => {
    if (user) {
      user.username = req.body.username
      user.name = req.body.name
      user.email = req.body.email
      user.affiliation = req.body.affiliation
      user.isMember = !!req.body.isMember
      user.isCurator = !!req.body.isCurator
      user.isAdmin = !!req.body.isAdmin
      return user.save()
    } else {
      error = true
      return res.status(400).send('Must provide a valid user id')
    }
  }).then(() => {
    res.status(200).send('User (' + req.body.id + ') updated')
  }).catch((err) => {
    if (!error) res.status(500).send(err.stack)
  })
}
