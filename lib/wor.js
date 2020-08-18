const config = require('./config')
const axios = require('axios')

module.exports = function () {
  let worUrl = config.get('webOfRegistriesUrl')
  worUrl = worUrl[worUrl.length - 1] !== '/' ? worUrl : worUrl.substring(0, worUrl.length - 1)

  return axios.get(worUrl + '/instances/').then(response => {
    let wor = config.get('webOfRegistries')

    response.data.forEach(registry => {
      if (!wor[registry['uriPrefix']]) {
        wor[registry['uriPrefix']] = registry['instanceUrl']
      }
    })

    config.set('webOfRegistries', wor)
    config.set('instances', response.data)
  })
}
