
var pug = require('pug')

const os = require('os')
const config = require('../../config')
const extend = require('xtend')

module.exports = function (req, res) {
  var locals = {
    platform: os.type(),
    architecture: os.arch(),
    osRelease: os.release(),
    nodeVersion: process.version,
    instanceName: config.get('instanceName'),
    frontendUrl: config.get('frontendURL'),
    instanceUrl: config.get('instanceUrl'),
    listenPort: config.get('port'),
    sparqlEndpoint: config.get('triplestore').sparqlEndpoint,
    graphStoreEndpoint: config.get('triplestore').graphStoreEndpoint,
    defaultGraph: config.get('triplestore').defaultGraph,
    graphPrefix: config.get('triplestore').graphPrefix,
    databasePrefix: config.get('databasePrefix'),
    removePublicEnabled: config.get('removePublicEnabled'),
    uploadLimit: config.get('uploadLimit'),
    resolveBatch: config.get('resolveBatch'),
    fetchLimit: config.get('fetchLimit'),
    staggeredQueryLimit: config.get('staggeredQueryLimit')
  }
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'application/json').send(JSON.stringify(locals))
  } else {
    locals = extend({
      config: config.get(),
      section: 'admin',
      adminSection: 'status',
      user: req.user
    }, locals)
    res.send(pug.renderFile('templates/views/admin/status.jade', locals))
  }
}
