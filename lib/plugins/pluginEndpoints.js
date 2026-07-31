const { default: axios } = require('axios')
const fs = require('fs')
const os = require('os')
const path = require('path')

const config = require('../config')

module.exports = function (req, res) {
  const name = req.body.name
  const endpoint = req.body.endpoint
  const category = req.body.category

  let pluginUrl = findPlugin(name, category)

  let prefix

  try {
    prefix = req.body.prefix
  } catch (error) {
    prefix = null
  }

  if (pluginUrl === null) {
    return res.status(404).send('The plugin ' + name + ' was not found or there is no url associated with this name. Check that this is a valid plugin name.')
  }

  switch (endpoint) {
    case 'status': {
      getStatus(pluginUrl, res)
      break
    }
    case 'evaluate': {
      const data = req.body.data
      getEvaluate(pluginUrl, data, category, res)
      break
    }
    case 'run': {
      const data = req.body.data
      getRun(pluginUrl, data, category, res)
      break
    }
    default: res.status(404).send('This plugin endpoint' + endpoint + ' is not known. Instead try status, evaluate, or run.')
  }

  function findPlugin (name, category) {
    const pluginList = config.get('plugins')[category]

    for (let plugin of pluginList) {
      if (plugin.name === name) {
        return plugin.url
      }
    }

    return null
  }

  function getStatus (pluginUrl, res) {
    return axios({
      method: 'GET',
      url: pluginUrl + 'status',
      responseType: 'text',
      timeout: 5000
    }).then(response => {
      return res.status(200).send(response.data)
    }).catch(error => {
      return res.status(500).send('The plugin ' + name + ' status endpoint is not responding. Check that the plugin is active and running. ' + error)
    })
  }

  function getEvaluate (pluginUrl, data, category, res) {
    var responseType

    switch (category) {
      case 'submit': {
        responseType = 'application/json'
        break
      }
      default: {
        responseType = 'text/plain'
        break
      }
    }

    return axios({
      headers: {
        'Content-Type': 'application/json',
        'Accepts': responseType,
        'Access-Control-Expose-Headers': 'Content-Disposition'
      },
      method: 'POST',
      url: pluginUrl + 'evaluate',
      data: data,
      timeout: 10000
    }).then(response => {
      if (category === 'submit') {
        res.header('Content-Type', 'application/json')
      }
      return res.status(200).send(response.data)
    }).catch(error => {
      return res.status(500).send('The plugin ' + name + ' evaluate endpoint is not responding. Check that the plugin is active and running. ' + error)
    })
  }

  function getRun (pluginUrl, data, category, res) {
    var pluginData
    var responseType
    let cleanup = null

    switch (category) {
      case 'rendering': {
        pluginData = getPublicDataFromURI(data)
        responseType = 'text'
        break
      }
      case 'download': {
        pluginData = getPublicDataFromURI(data)
        responseType = 'arraybuffer'
        break
      }
      case 'submit': {
        const prepared = prepareSubmitPluginData(data)
        pluginData = prepared.pluginData
        cleanup = prepared.cleanup
        responseType = 'arraybuffer'
        break
      }
    }

    return axios({
      headers: {
        'Content-Type': 'application/json'
      },
      responseType: responseType,
      method: 'POST',
      url: pluginUrl + 'run',
      data: pluginData,
      timeout: 60000
    }).then(response => {
      if (category === 'download') {
        const filename = response.headers['content-disposition'].split('=')[1] || 'downloaded_file'
        res.header('Content-Disposition', 'attachment; filename="' + filename + '"')
        res.header('Content-Type', 'application/octet-stream')
        res.header('Access-Control-Expose-Headers', 'Content-Disposition')
      }
      return res.status(200).send(response.data)
    }).catch(error => {
      res.header('Content-Type', 'text/plain')
      return res.status(500).send(error)
    }).finally(() => {
      if (cleanup) {
        cleanup()
      }
    })
  }

  function prepareSubmitPluginData (data) {
    let parsedData = data

    if (typeof parsedData === 'string') {
      try {
        parsedData = JSON.parse(parsedData)
      } catch (error) {
        return {
          pluginData: data,
          cleanup: null
        }
      }
    }

    const manifestFiles = parsedData && parsedData.manifest && Array.isArray(parsedData.manifest.files)
      ? parsedData.manifest.files
      : null

    if (!manifestFiles) {
      return {
        pluginData: parsedData,
        cleanup: null
      }
    }

    const hasInlineContent = manifestFiles.some(file => typeof file.contentBase64 === 'string' && file.contentBase64.length > 0)

    if (!hasInlineContent) {
      return {
        pluginData: parsedData,
        cleanup: null
      }
    }

    const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'sbh-submit-plugin-'))
    const preparedFiles = manifestFiles.map((file, index) => {
      if (typeof file.contentBase64 === 'string' && file.contentBase64.length > 0) {
        const safeFilename = path.basename(file.filename || `plugin_input_${index}`)
        const localPath = path.join(tempDir, safeFilename)
        fs.writeFileSync(localPath, Buffer.from(file.contentBase64, 'base64'))
        return {
          ...file,
          url: localPath
        }
      }
      return file
    })

    return {
      pluginData: {
        ...parsedData,
        manifest: {
          ...parsedData.manifest,
          files: preparedFiles
        }
      },
      cleanup: () => {
        try {
          fs.rmSync(tempDir, { recursive: true, force: true })
        } catch (error) {
          // best effort cleanup
        }
      }
    }
  }

  function getPublicDataFromURI (data) {
    let uri

    if (prefix) {
      uri = prefix + data.uriSuffix
    } else {
      uri = config.get('instanceUrl') + data.uriSuffix
    }
    const pluginData = {
      ...data,
      complete_sbol: `${uri}/sbol`,
      shallow_sbol: `${uri}/sbolnr`,
      genbank: `${uri}/gb`,
      top_level: data.top
    }

    return pluginData
  }
}
