const { default: axios } = require('axios')

const config = require('../config')

module.exports = function (req, res) {
  const name = req.query.name
  const endpoint = req.query.endpoint
  const category = req.query.category
  const pluginUrl = findPlugin(name, category)

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
    default: res.status(400).send('This plugin endpoint' + endpoint + ' is not known. Instead try status, evaluate, or run.')
  }

  function findPlugin (name, category) {
    const pluginList = config.get('plugins')

    switch (category) {
      case 'visual': {
        for (let plugin of pluginList.rendering) {
          if (plugin.name === name) {
            return plugin.url
          }
        }
        break
      }
      case 'download': {
        for (let plugin of pluginList.download) {
          if (plugin.name === name) {
            return plugin.url
          }
        }
        break
      }
      case 'submit': {
        for (let plugin of pluginList.submit) {
          if (plugin.name === name) {
            return plugin.url
          }
        }
        break
      }
      case 'curation': {
        for (let plugin of pluginList.curation) {
          if (plugin.name === name) {
            return plugin.url
          }
        }
        break
      }
      case 'authorization': {
        for (let plugin of pluginList.authorization) {
          if (plugin.name === name) {
            return plugin.url
          }
        }
        break
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
      return res.status(400).send('The plugin ' + name + ' status endpoint is not responding. Check that the plugin is active and running. ' + error)
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
      return res.status(200).send(response.data)
    }).catch(error => {
      return res.status(400).send('The plugin ' + name + ' evaluate endpoint is not responding. Check that the plugin is active and running. ' + error)
    })
  }

  function getRun (pluginUrl, data, category, res) {
    var pluginData
    var responseType

    switch (category) {
      case 'visual': {
        pluginData = getPublicDataFromURI(data)
        responseType = 'text/html'
        break
      }
      case 'download': {
        pluginData = getPublicDataFromURI(data)
        responseType = 'application/octet-stream'
        break
      }
      case 'submit': {
        pluginData = data
        responseType = 'application/octet-stream'
        break
      }
    }

    return axios({
      headers: {
        'Content-Type': 'application/json',
        'Accepts': responseType
      },
      method: 'POST',
      url: pluginUrl + 'run',
      data: pluginData
    }).then(response => {
      return res.status(200).send(response.data)
    }).catch(error => {
      return res.status(error.status).send(error)
      // return res.status(400).send('The plugin ' + name + ' run endpoint is not responding. Check that the plugin is active and running.');
    })
  }

  function getPublicDataFromURI (data) {
    const uri = data.uri
    const pluginData = {
      ...data,
      complete_sbol: `${uri}/sbol`,
      shallow_sbol: `${uri}/sbolnr`,
      genbank: `${uri}/gb`
    }

    return pluginData
  }
}
