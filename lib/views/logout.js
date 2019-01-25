
var pug = require('pug')

module.exports = function (req, res) {
  if (req.session.user !== undefined) { delete req.session.user }
  req.session.save(() => {
    res.redirect('/')
  })
}
