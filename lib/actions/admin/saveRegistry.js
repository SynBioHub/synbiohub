const config = require('../../config')

module.exports = function (req, res) {
  var registries = config.get('webOfRegistries')

  if (!req.body.url) {
    return res.status(400).header('content-type', 'text/plain').send('Must provide a valid registry URL')
  }
  if (!req.body.uri) {
    return res.status(400).header('content-type', 'text/plain').send('Must provide a valid registry URI')
  }

  registries[req.body.uri] = req.body.url

  config.set('webOfRegistries', registries)

  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'text/plain').send('Registry (' + req.body.uri + ', ' + req.body.url + ') saved successfully')
  } else {
    res.redirect('/admin/registries')
  }
}
