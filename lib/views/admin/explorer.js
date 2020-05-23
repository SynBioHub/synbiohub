const pug = require('pug')

const config = require('../../config')
const request = require('request')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    post(req, res)
  } else {
    form(req, res)
  }
}

function getExplorerConfig () {
  return new Promise((resolve, reject) => {
    request({
      method: 'GET',
      url: config.get('SBOLExplorerEndpoint') + 'config',
      json: true
    }, function (error, response, body) {
      if (error) {
        reject(error)
      }
      resolve(body)
    })
  })
}

function setExplorerConfig (explorerConfig) {
  console.log(JSON.stringify(explorerConfig))
  return new Promise((resolve, reject) => {
    request({
      method: 'POST',
      url: config.get('SBOLExplorerEndpoint') + 'config',
      headers: { 'content-type': 'application/json' },
      json: true,
      body: explorerConfig
    }, function (error, response, body) {
      if (error) {
        reject(error)
      }
      resolve(body)
    })
  })
}

function getExplorerCron () {
  return new Promise((resolve, reject) => {
    request({
      method: 'GET',
      url: config.get('SBOLExplorerEndpoint') + 'cron',
      json: true
    }, function (error, response, body) {
      if (error) {
        reject(error)
      }
      resolve(body)
    })
  })
}

function setExplorerCron (cron) {
  return new Promise((resolve, reject) => {
    request({
      method: 'POST',
      url: config.get('SBOLExplorerEndpoint') + 'cron',
      json: true,
      body: { 'cron': cron }
    }, function (error, response, body) {
      if (error) {
        reject(error)
      }
      resolve(body)
    })
  })
}

function form (req, res) {
  var locals = {
    config: config.get(),
    section: 'admin',
    adminSection: 'explorer',
    user: req.user
  }

  getExplorerConfig().then((body) => {
    locals.explorerConfig = body
    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'application/json').send(JSON.stringify(body))
    } else {
      res.send(pug.renderFile('templates/views/admin/explorer.jade', locals))
    }
  }).catch(() => {
    if (config.get('useSBOLExplorer')) {
      if (!req.accepts('text/html')) {
        return res.status(503).header('content-type', 'text/plain').send('SBOLExplorer is down')
      } else {
        res.send(pug.renderFile('templates/views/admin/explorerDown.jade', locals))
      }
    } else {
      if (!req.accepts('text/html')) {
        return res.status(404).header('content-type', 'text/plain').send('SBOLExplorer is not in use')
      } else {
        res.send(pug.renderFile('templates/views/admin/explorerOff.jade', locals))
      }
    }
  })
}

function post (req, res) {
  console.log(JSON.stringify(req.body))

  if (req.body.SBOLExplorerEndpoint !== undefined && req.body.SBOLExplorerEndpoint !== '' && req.body.SBOLExplorerEndpoint.endsWith('/')) {
    config.set('SBOLExplorerEndpoint', req.body.SBOLExplorerEndpoint)
  }

  if (req.body.useSBOLExplorer) {
    config.set('useSBOLExplorer', true)
  } else {
    config.set('useSBOLExplorer', false)
  }

  getExplorerCron().then((cron) => {
    // config.set('use_cron', req.body.useCron == 'on')

    var cronString = cron['cron']
    if (req.body.useCron && req.body.cronDay !== undefined && req.body.cronHour !== undefined) {
      var localCronString = `* ${req.body.cronHour} ${req.body.cronDay} * *`
      console.log('CRON STRING:' + localCronString)
      if (cronString !== localCronString) {
        // config.set('cronDay', req.body.cronDay)
        // config.set('cronHour', req.body.cronHour)
        setExplorerCron(localCronString)
      }
    } else {
      setExplorerCron('#')
    }
  })

  getExplorerConfig().then((explorerConfig) => {
    if (req.body.whichSearch === 'usearch') {
      explorerConfig.which_search = 'usearch'
    } else if (req.body.whichSearch === 'vsearch') {
      explorerConfig.which_search = 'vsearch'
    }

    if (req.body.useDistributedSearch) {
      explorerConfig.distributed_search = true
    } else {
      explorerConfig.distributed_search = false
    }

    if (req.body.pagerankTolerance !== undefined && req.body.pagerankTolerance !== '') {
      explorerConfig.pagerank_tolerance = req.body.pagerankTolerance
    }

    if (req.body.uclustIdentity !== undefined && req.body.uclustIdentity !== '') {
      explorerConfig.uclust_identity = req.body.uclustIdentity
    }

    if (req.body.elasticsearchEndpoint !== undefined && req.body.elasticsearchEndpoint !== '' && req.body.elasticsearchEndpoint.endsWith('/')) {
      explorerConfig.elasticsearch_endpoint = req.body.elasticsearchEndpoint
    }

    if (req.body.elasticsearchIndexName !== undefined && req.body.elasticsearchIndexName !== '') {
      explorerConfig.elasticsearch_index_name = req.body.elasticsearchIndexName
    }

    if (req.body.sparqlEndpoint !== undefined && req.body.sparqlEndpoint !== '' && req.body.sparqlEndpoint.endsWith('/sparql?')) {
      explorerConfig.sparql_endpoint = req.body.sparqlEndpoint
    }

    return setExplorerConfig(explorerConfig)
  }).then(() => {
    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'text/plain').send('SBOLExplorer configuration updated successfully')
    } else {
      form(req, res)
    }
  }).catch(() => {
    if (!req.accepts('text/html')) {
      return res.status(500).header('content-type', 'text/plain').send('Error updating SBOLExplorer configuration')
    } else {
      form(req, res)
    }
  })
}
