const config = require('../../config')
const request = require('request')

module.exports = function (req, res) {
  sendIndexUpdateRequest()
  res.redirect('/admin/explorer')
}

function sendIndexUpdateRequest () {
  console.log('Sending index update request to SBOLExplorer.')
  request({
    method: 'GET',
    url: config.get('SBOLExplorerEndpoint') + 'update'
  }, function (error, response, body) {
    if (error) {
      console.log(error)
    } else {
      console.log(body)
    }
  })
}
