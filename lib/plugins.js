const config = require('./config')
const extend = require('xtend')
const request = require('request').defaults({ encoding: null })
const util = require('util')

// Promisify the POST function
const post = util.promisify(request.post)
const get = util.promisify(request.get)

/**
 * The get*Plugins functions run the status check on plugins
 * described in the config file, and then return only those
 * which are online.
 */

function getSubmitPlugins () {
  return getPlugins('submit')
}

function getVisualPlugins () {
  return getPlugins('rendering')
}

function getDownloadPlugins () {
  return getPlugins('download')
}

/**
 * Actually test the status endpoint to make sure that
 * the plugin can server requests
 */
async function getPlugins (type) {
  let plugins = config.get('plugins')[type]

  let statuses = await Promise.all(plugins.map(plugin => {
    let statusUrl = plugin.url + 'status'
    return get(statusUrl)
      .then(response => response.statusCode < 300)
      .catch(err => {
        console.error(err)
        return false
      })
  }))

  // Only return plugins which passed the check.
  return plugins.filter((plugin, idx) => statuses[idx])
}

function testSubmitPlugin (plugin) {
  // let evaluateUrl = plugin.url + 'evaluate'
  throw new Error("Can't test submit plugins yet.")
}

function testVisualPlugin (plugin, type) {
  let evaluateUrl = plugin.url + 'evaluate'
  let evaluateParams = {
    type: type
  }

  return post(evaluateUrl, { json: true, body: evaluateParams })
    .then(response => response.statusCode < 300)
    .catch(err => {
      console.error(err)
      return false
    })
}

function testDownloadPlugin (plugin, type) {
  let evaluateUrl = plugin.url + 'evaluate'
  let evaluateParams = {
    type: type
  }

  return post(evaluateUrl, { json: true, body: evaluateParams })
    .then(response => response.statusCode < 300)
    .catch(err => {
      console.error(err)
      return false
    })
}

function runSubmitPlugin (plugin, body) {
  throw new Error("Can't run submit plugins yet.")
}

function runVisualPlugin (plugin, body) {
  let instanceData = {
    instanceUrl: config.get('instanceUrl')
  }

  body = extend(body, instanceData)

  return runPlugin(plugin, body)
}

function runDownloadPlugin (plugin, body) {
  let instanceData = {
    instanceUrl: config.get('instanceUrl')
  }

  body = extend(body, instanceData)

  return runPlugin(plugin, body)
}

function runPlugin (plugin, body) {
  let runUrl = plugin.url + 'run'

  let postParams = {
    json: true,
    body: body
  }

  return post(runUrl, postParams)
    .then(response => {
      if (response.statusCode === 400) {
        return {
          remove: true
        }
      }

      if (response.statusCode >= 300) {
        throw new Error('Plugin run failed!')
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
}

module.exports = {
  getSubmitPlugins: getSubmitPlugins,
  getVisualPlugins: getVisualPlugins,
  getDownloadPlugins: getDownloadPlugins,
  testSubmitPlugin: testSubmitPlugin,
  testVisualPlugin: testVisualPlugin,
  testDownloadPlugin: testDownloadPlugin,
  runSubmitPlugin: runSubmitPlugin,
  runVisualPlugin: runVisualPlugin,
  runDownloadPlugin: runDownloadPlugin
}
