const config = require('../../config')
const request = require('request')

module.exports = function (req, res) {
  console.log('Getting indexing log')
  request({
    method: 'GET',
    url: config.get('SBOLExplorerEndpoint') + 'indexinginfo'
  }, function (error, response, body) {
    if (error) {
      console.log(error)
    } else {
      res.attachment('explorer_indexing_log.txt')
      res.type('txt')
      res.send(body)
    }
  })
}
