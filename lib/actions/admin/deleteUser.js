
const db = require('../../db')

module.exports = function (req, res) {
  db.model.User.findById(req.body.id).then((user) => {
    if (user) {
      user.destroy()
    } else {
      res.status(400).send('Must provide a valid user id')
    }
  }).then(() => {
    res.status(200).send('user (' + req.body.id + ') deleted')
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}
