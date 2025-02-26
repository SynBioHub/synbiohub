const { default: axios } = require('axios')

const config = require('../config')

module.exports = function (req, res) {
  const name = req.query.name
  const endpoint = req.query.endpoint
  const category = req.query.category

  if (category === 'message') {
    console.log(req.body.message)
    return res.status(200).send('Message sent: ' + req.body.message)
  }
  let pluginUrl = findPlugin(name, category)

  const prefix = req.query.prefix
  // Insert prefix into pluginUrl
  if (prefix) {
    pluginUrl = prefix + pluginUrl
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
      const data = JSON.parse(decodeURIComponent(req.query.data))
      getEvaluate(pluginUrl, data, category, res)
      break
    }
    case 'run': {
      const data = JSON.parse(decodeURIComponent(req.query.data))
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
      responseType: 'text'
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
        'Accepts': responseType
      },
      method: 'POST',
      url: pluginUrl + 'evaluate',
      data: data
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
        pluginData = data
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
      data: pluginData
    }).then(response => {
      if (category === 'download') {
        const filename = response.headers['content-disposition'].split('=')[1]
        res.header('Content-Disposition', 'attachment; filename="' + filename + '"')
        res.header('Content-Type', 'application/octet-stream')
        res.header('Access-Control-Expose-Headers', 'Content-Disposition')
      }
      if (category === 'submit') {
        res.header('Content-Type', 'application/zip')
        const contentHeader = response.headers['content-disposition']
        res.header('Content-Disposition', contentHeader)
        res.header('Cache-Control', 'no-cache')
      }
      return res.status(200).send(response.data)
    }).catch(error => {
      res.header('Content-Type', 'text/plain')
      return res.status(500).send(error)
    })
  }

  function getPublicDataFromURI (data) {
    let uri = data.uri
    if (prefix) {
      uri = prefix + uri
    }
    const pluginData = {
      ...data,
      complete_sbol: `${uri}/sbol`,
      shallow_sbol: `${uri}/sbolnr`,
      genbank: `${uri}/gb`,
      top_level: uri
    }

    return pluginData
  }
}
