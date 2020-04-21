
const config = require('../../config')
const updateWor = require('./updateWor')

module.exports = function (req, res) {
  if (req.body.administratorEmail) {
    config.set('administratorEmail', req.body.administratorEmail)

    updateWor()

    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'text/plain').send('Updated administrator email')
    } else {
      res.redirect('/admin/registries')
    }
  } else {
    return res.status(400).header('content-type', 'text/plain').send('Must provide administrator email')
  }
}
