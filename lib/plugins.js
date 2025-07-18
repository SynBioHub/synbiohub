const config = require('./config')
const extend = require('xtend')
const request = require('request').defaults({ encoding: null })
const util = require('util')
const mime = require('mime-types')
const expose = require('./api/expose')
const tmp = require('tmp-promise')
const fs = require('fs')

// Promisify the POST function
const post = util.promisify(request.post)
const get = util.promisify(request.get)

function buildSubmissionManifest (filename) {
  let instanceUrl = config.get('instanceUrl')
  let type = mime.lookup(filename)
  let exposeId = expose.createExpose(filename)
  let exposeUrl = `${instanceUrl}expose/${exposeId}`

  let manifest = { files: [] }

  // Should produce manifest for ZIPs, but right now just put in the zip
  manifest.files.push({
    filename: filename.substring(filename.lastIndexOf('/') + 1),
    type: type,
    url: exposeUrl
  })

  return manifest
}

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
    return get({ url: statusUrl, timeout: 5000 }) // 5 seconds timeout (Can be adjusted as needed)
      .then(response => response.statusCode < 300)
      .catch(err => {
        console.error(err)
        return false
      })
  }))

  // Only return plugins which passed the check.
  return plugins.filter((plugin, idx) => statuses[idx])
}
function testSubmitPlugin (plugin, filename) {
  let evaluateUrl = plugin.url + 'evaluate'
  let manifest = buildSubmissionManifest(filename)
  let evaluateParams = {
    manifest: manifest
  }

  return post(evaluateUrl, { json: true, body: evaluateParams })
    .then(response => response.statusCode < 300)
    .catch(err => {
      console.error(err)
      return false
    })
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

async function runSubmitPlugin (plugin, filename) {
  let manifest = buildSubmissionManifest(filename)
  let runParams = {
    manifest: manifest,
    instanceUrl: config.get('instanceUrl')
  }

  let result = await runPlugin(plugin, runParams)
  let tmpFile = await tmp.file()
  console.log(JSON.stringify(tmpFile))
  await fs.writeSync(tmpFile.fd, result.body)

  return tmpFile.path
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
        throw new Error('Plugin run failed: ' + response.statusCode + ' ' + response.statusMessage)
      }

      let filename = '' // Assume no filename

      // Look for filename in content disposition header
      let contentDisposition = response.headers['content-disposition']
      if (contentDisposition && contentDisposition.indexOf('filename=') >= 0) {
        filename = contentDisposition.substring(contentDisposition.indexOf('filename=') + 9)
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
