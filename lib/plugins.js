const config = require('./config')
const extend = require('xtend')
const request = require('request').defaults({ encoding: null })
const util = require('util')
const stream = require('./api/stream')

// Promisify the POST function
const post = util.promisify(request.post)
const get = util.promisify(request.get)

function callPlugin (plugin, requestData) {
  let runUrl = plugin.url + 'run'
  let statusUrl = plugin.url + 'status'

  let instanceData = {
    instanceUrl: config.get('instanceUrl')
  }

  requestData = extend(requestData, instanceData)
  console.debug(`Sending ${JSON.stringify(requestData)} to ${runUrl}`)

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

      let filename = '' // Assume no filename

      // Look for filename in content disposition header
      let contentDisposition = response.headers['content-disposition']
      if (contentDisposition) {
        let filenameRegex = RegExp('filename="(?<filename>.*)"')
        let matches = contentDisposition.match(filenameRegex).groups

        filename = matches.filename
        console.log(filename)
      }

      return {
        body: response.body,
        filename: filename
      }
    })

  return stream.create(pluginResponse)
}

module.exports = {
  callPlugin: callPlugin
}
