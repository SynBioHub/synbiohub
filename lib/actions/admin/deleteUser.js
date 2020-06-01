
const db = require('../../db')

module.exports = function (req, res) {
  db.model.User.findById(req.body.id).then((user) => {
    if (req.body.id === '1') {
      res.status(422).send('Primary admin user cannot be deleted')
    } else if (user) {
      user.destroy().then(() => {
        res.status(200).send('user (' + req.body.id + ') deleted')
      })
    } else {
      res.status(400).send('Must provide a valid user id')
    }
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}
