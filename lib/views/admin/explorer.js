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

function form (req, res) {
  var locals = {
    config: config.get(),
    section: 'admin',
    adminSection: 'explorer',
    user: req.user
  }

  getExplorerConfig().then((body) => {
    locals.explorerConfig = body

    res.send(pug.renderFile('templates/views/admin/explorer.jade', locals))
  }).catch(() => {
    res.send(pug.renderFile('templates/views/admin/explorerDown.jade', locals))
  })
}

function post (req, res) {
  console.log(req.body)

  if (req.body.SBOLExplorerEndpoint !== undefined && req.body.SBOLExplorerEndpoint !== '' && req.body.SBOLExplorerEndpoint.endsWith('/')) {
    config.set('SBOLExplorerEndpoint', req.body.SBOLExplorerEndpoint)
  }

  if (req.body.useSBOLExplorer) {
    config.set('useSBOLExplorer', true)
  } else {
    config.set('useSBOLExplorer', false)
  }

  getExplorerConfig().then((explorerConfig) => {
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

    if (req.body.synbiohubPublicGraph !== undefined && (req.body.synbiohubPublicGraph === '' || req.body.synbiohubPublicGraph.endsWith('/public'))) {
      explorerConfig.synbiohub_public_graph = req.body.synbiohubPublicGraph
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
    form(req, res)
  }).catch(() => {
    form(req, res)
  })
}
