
var config = require('../config')

var httpProxy = require('http-proxy')

var proxy = httpProxy.createProxyServer({})

module.exports = function(req, res) {

    proxy.web(req, res, {
        target: config.get('backendURL')
    })

}


