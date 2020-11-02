const config = require('../../config')
const request = require('request')

module.exports = function (req, res) {
  sendIndexUpdateRequest()
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'text/plain').send('Sending index update request to SBOLExplorer')
  } else {
    res.redirect('/admin/explorer')
  }
}

function sendIndexUpdateRequest () {
  console.log('Sending index update request to SBOLExplorer.')
  request({
    method: 'GET',
    url: config.get('SBOLExplorerEndpoint') + 'update?prefix=' + Buffer.from(config.get('databasePrefix')).toString('base64') + '&'
  }, function (error, response, body) {
    if (error) {
      console.log(error)
    } else {
      console.log(body)
    }
  })
}
