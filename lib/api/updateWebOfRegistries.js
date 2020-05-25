const config = require('../config')
const request = require('request')
const util = require('util')
const get = util.promisify(request.get)

module.exports = async function (req, res) {
  let worUrl = config.get('webOfRegistriesUrl')
  let current = config.get('webOfRegistries')

  let instances = await get(worUrl + '/instances')
  let registries = JSON.parse(instances.body)

  if (registries && registries instanceof Array) {
    registries.forEach(registry => {
      if (registry.instanceUrl.endsWith('/')) { current[registry.uriPrefix] = registry.instanceUrl.substr(0, registry.instanceUrl.length - 1) } else { current[registry.uriPrefix] = registry.instanceUrl }
    })
    config.set('webOfRegistries', current)
  }

  res.status(200).send('Registries Updated')
}
