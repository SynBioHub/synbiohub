const config = require('../config')

module.exports = function (req, res) {
  let registries = req.body
  let current = config.get('webOfRegistries')

  if (registries && registries instanceof Array) {
    registries.forEach(registry => {
      if (registry.instanceUrl.endsWith('/')) { current[registry.uriPrefix] = registry.instanceUrl.substr(0, registry.instanceUrl.length - 1) } else { current[registry.uriPrefix] = registry.instanceUrl }
    })
    config.set('webOfRegistries', current)
  }

  res.status(200).send('Registries Updated')
}
