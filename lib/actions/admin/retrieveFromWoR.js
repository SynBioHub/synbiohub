const getRegistries = require('../../wor')

module.exports = function (req, res) {
  return getRegistries().then(() => {
    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'text/plain').send('Retrieved registries from Web-of-Registries')
    } else {
      res.redirect('/admin/registries')
    }
  })
}
