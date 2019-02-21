const request = require('request')
const util = require('util')
const stream = require('./api/stream')

// Promisify the POST function
const post = util.promisify(request.post)
const get = util.promisify(request.get)

function callPlugin (plugin, requestData) {
  let runUrl = plugin.url + 'run'
  let statusUrl = plugin.url + 'status'

  let postParams = {
    json: true,
    body: requestData
  }

  let pluginResponse = get(statusUrl)
    .then(response => {
      if (response.statusCode >= 300) {
        throw new Error('Plugin status error!')
      }

      return post(runUrl, postParams)
    }).then(response => {
      if (response.statusCode >= 300) {
        throw new Error('Plugin run error!')
      }

      return response
    })

  return stream.create(pluginResponse)
}

module.exports = {
  callPlugin: callPlugin
}
