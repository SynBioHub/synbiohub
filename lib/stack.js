
var StackFrontend = require('sbolstack-frontend')
var config = require('config')

module.exports = new StackFrontend(
    config.get('backendURL'),
    config.get('backendUser'),
    config.get('backendPassword')
)


