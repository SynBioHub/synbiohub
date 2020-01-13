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
  let evaluateUrl = plugin.url + 'evaluate'

  let type = requestData.type

  let instanceData = {
    instanceUrl: config.get('instanceUrl')
  }

  requestData = extend(requestData, instanceData)

  let postParams = {
    json: true,
    body: requestData
  }

  let evaluateParams = {
    json: true,
    body: { type: type }
  }

  let pluginResponse = get(statusUrl)
    .then(response => {
      if (response.statusCode >= 300) {
        throw new Error('Plugin status failed!')
      }

      return post(evaluateUrl, evaluateParams)
    }).then(response => {
      if (response.statusCode >= 300) {
        throw new Error('Plugin evaluate failed!')
      }

      return post(runUrl, postParams)
    }).then(response => {
      if (response.statusCode >= 300) {
        if (response.statusCode === 400) {
          // This code means we should disappear the box/link/whatever.
          throw new Error('Plugin run failed!')
        }

        return {
          body: response.body
        }
      }

      let filename = '' // Assume no filename

      // Look for filename in content disposition header
      let contentDisposition = response.headers['content-disposition']
      if (contentDisposition) {
        let filenameRegex = RegExp('filename="(?<filename>.*)"')
        let matches = contentDisposition.match(filenameRegex).groups

        filename = matches.filename
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
