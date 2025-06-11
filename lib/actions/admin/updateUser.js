
const db = require('../../db')

module.exports = function (req, res) {
  db.model.User.findById(req.body.id).then((user) => {
    if (user) {
      user.name = req.body.name
      user.email = req.body.email
      user.affiliation = req.body.affiliation
      user.isAdmin = (req.body.isAdmin !== undefined && req.body.isAdmin !== 'false') ||
        req.body.id === '1'
      user.isCurator = req.body.isCurator !== undefined && req.body.isCurator !== 'false'
      user.isMember = req.body.isMember !== undefined && req.body.isMember !== 'false'
      user.save().then(() => {
        res.status(200).send('User (' + req.body.id + ') updated')
      })
    } else {
      res.status(400).send('Must provide a valid user id')
    }
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}
