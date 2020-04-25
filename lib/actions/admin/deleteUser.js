
const db = require('../../db')

module.exports = function (req, res) {
  var error = false
  db.model.User.findById(req.body.id).then((user) => {
    if (user) {
      user.destroy()
    } else {
      error = true
      res.status(400).send('Must provide a valid user id')
    }
  }).then(() => {
    res.status(200).send('user (' + req.body.id + ') deleted')
  }).catch((err) => {
    if (!error) res.status(500).send(err.stack)
  })
}
