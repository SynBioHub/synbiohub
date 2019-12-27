const config = require('../../config')
const request = require('request')

module.exports = function (req, res) {
  console.log('Getting log')
  request({
    method: 'GET',
    url: config.get('SBOLExplorerEndpoint') + 'info'
  }, function (error, response, body) {
    if (error) {
      console.log(error)
    } else {
      res.attachment('explorer_log.txt')
      res.type('txt')
      res.send(body)
    }
  })
}
