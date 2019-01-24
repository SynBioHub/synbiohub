const axios = require('axios')
const config = require('./lib/config')

const url = 'http://localhost:' + config.get('port') + '/'

axios.get(url).then(response => {
  return process.exit(0)
}).catch(error => {
  return process.exit(1)
})
