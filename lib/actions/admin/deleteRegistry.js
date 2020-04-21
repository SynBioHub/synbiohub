
const config = require('../../config')

module.exports = function (req, res) {
  const uri = req.body.uri
  var registries = config.get('webOfRegistries')

  if (!uri) {
    return res.status(400).header('content-type', 'text/plain').send('Must provide a valid registry URI')
  }
  if (!registries) {
    return res.status(500).header('content-type', 'text/plain').send('No registries to delete')
  }
  if (!registries[uri]) {
    return res.status(404).header('content-type', 'text/plain').send('Registry URI not found')
  }

  delete registries[uri]

  config.set('webOfRegistries', registries)

  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'text/plain').send('Registry (' + uri + ') deleted successfully')
  } else {
    res.redirect('/admin/registries')
  }
}
