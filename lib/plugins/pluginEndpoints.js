const { default: axios } = require('axios')

const config = require('../config')

module.exports = function (req, res) {
  const name = req.query.name
  const endpoint = req.query.endpoint
  const [pluginUrl, category] = findPlugin(name)

  if (pluginUrl === null) {
    return res.status(404).send('The plugin ' + name + ' was not found or there is no url associated with this name. Check that this is a valid plugin name.')
  }

  switch (endpoint) {
    case 'status': {
      getStatus(pluginUrl, res)
      break
    }
    case 'evaluate': {
      const data = decodeURIComponent(req.query.data)
      getEvaluate(pluginUrl, data, category, res)
      break
    }
    case 'run': {
      const data = decodeURIComponent(req.query.data)
      getRun(pluginUrl, data, category, res)
      break
    }
    case 'save': {
      const data = decodeURIComponent(req.query.data)
      getSave(pluginUrl, data, res)
      break
    }
    default: res.status(400).send('This plugin endpoint' + endpoint + ' is not known. Instead try status, evaluate, or run.')
  }

  function findPlugin (name) {
    const pluginList = config.get('plugins')

    for (let plugin of pluginList.download) {
      if (plugin.name === name) {
        return [plugin.url, 'download']
      }
    }

    for (let plugin of pluginList.rendering) {
      if (plugin.name === name) {
        return [plugin.url, 'visual']
      }
    }

    for (let plugin of pluginList.submit) {
      if (plugin.name === name) {
        return [plugin.url, 'submit']
      }
    }

    for (let plugin of pluginList.curation) {
      if (plugin.name === name) {
        return [plugin.url, 'curation']
      }
    }

    for (let plugin of pluginList.authorization) {
      if (plugin.name === name) {
        return [plugin.url, 'authorization']
      }
    }

    return [null, null]
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
    return axios({
      method: 'POST',
      url: pluginUrl + 'evaluate',
      responseType: category === 'submit' ? 'application/json' : category === 'curation' ? 'application/json' : 'text',
      data: data
    }).then(response => {
      return res.status(200).send(response.data)
    }).catch(error => {
      return res.status(400).send('The plugin ' + name + ' evaluate endpoint is not responding. Check that the plugin is active and running. ' + error)
    })
  }

  function getRun (pluginUrl, data, category, res) {
    return axios({
      method: 'POST',
      url: pluginUrl + 'run',
      responseType: category === 'visual' ? 'text' : category === 'submit' ? 'arraybuffer' : category === 'curation' ? 'application/json' : 'blob',
      data: data
    }).then(response => {
      return res.status(200).send(response.data)
    }).catch(error => {
      return res.status(error.status).send(error)
      // return res.status(400).send('The plugin ' + name + ' run endpoint is not responding. Check that the plugin is active and running.');
    })
  }

  function getSave (pluginUrl, data, res) {
    return axios({
      method: 'POST',
      url: pluginUrl + 'save',
      responseType: 'text',
      data: data
    }).then(response => {
      return res.status.send(response.data)
    }).catch(error => {
      return res.status(400).send('The plugin ' + name + ' save endpoint is not responding. Check that the plugin is active and running. ' + error)
    })
  }
}
