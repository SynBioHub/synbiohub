const pug = require('pug')

const config = require('../../config')
const request = require('request')

const explorerConfigEndpoint = 'http://localhost:13162/config'
const explorerUpdateEndpoint = 'http://localhost:13162/update'

module.exports = function(req, res) {
    if(req.method === 'POST') {
        post(req, res)
    } else {
        form(req, res)
    }
}


function getExplorerConfig() {
    return new Promise((resolve, reject) => {
        request({
            method: 'GET',
            url: explorerConfigEndpoint,
            json: true
        }, function (error, response, body) {
            resolve(body)
        })
    })
}


function setExplorerConfig(explorerConfig) {
    return new Promise((resolve, reject) => {
        request({
            method: 'POST',
            url: explorerConfigEndpoint,
            headers: { 'content-type': 'application/json'},
            json: true,
            body: explorerConfig
        }, function (error, response, body) {
            resolve(body)
        })
    })
}


function sendIndexUpdateRequest() {
    request({
        method: 'GET',
        url: explorerUpdateEndpoint
    }, function (error, response, body) {
        console.log(body)
    })
}


function form(req, res) {
    getExplorerConfig().then(function(body) {
        const locals = {
            config: config.get(),
            section: 'admin',
            adminSection: 'explorer',
            user: req.user,
            explorerConfig: body
        }
	
        return Promise.resolve(locals)
    }).then(function(locals) {
        res.send(pug.renderFile('templates/views/admin/explorer.jade', locals))
    }).catch(() => {
        const locals = {
            config: config.get(),
            section: 'admin',
            adminSection: 'explorer',
            user: req.user
        }

        res.send(pug.renderFile('templates/views/admin/explorerDown.jade', locals))
    })
}


function post(req, res) {
    console.log(req.body)

    getExplorerConfig().then(function(explorerConfig) {
        if (req.body.manualReindex) {
            console.log('sending index update request')
            sendIndexUpdateRequest()
        }

        if (req.body.useDistributedSearch) {
            explorerConfig.distributed_search = true
        } else {
            explorerConfig.distributed_search = false
        }

        if(req.body.pagerankTolerance !== undefined && req.body.pagerankTolerance !== "") {
            explorerConfig.pagerank_tolerance = req.body.pagerankTolerance
        }

        if(req.body.uclustIdentity !== undefined && req.body.uclustIdentity !== "") {
            explorerConfig.uclust_identity = req.body.uclustIdentity
        }

        if(req.body.elasticsearchEndpoint !== undefined && req.body.elasticsearchEndpoint !== "") {
            explorerConfig.elasticsearch_endpoint = req.body.elasticsearchEndpoint
        }

        if(req.body.elasticsearchIndexName !== undefined && req.body.elasticsearchIndexName !== "") {
            explorerConfig.elasticsearch_index_name = req.body.elasticsearchIndexName
        }

        if(req.body.sparqlEndpoint !== undefined && req.body.sparqlEndpoint !== "") {
            explorerConfig.sparql_endpoint = req.body.sparqlEndpoint
        }

        return setExplorerConfig(explorerConfig)
    }).then(() => {
        form(req, res);
    }).catch(() => {
        form(req, res);
    })
}

