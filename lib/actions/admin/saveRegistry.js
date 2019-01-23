const config = require('../../config')
const extend = require('xtend')

module.exports = function (req, res) {
  var registries = config.get('webOfRegistries')

  registries[req.body.uri] = req.body.url

  config.set('webOfRegistries', registries)

  res.redirect('/admin/registries')
}
