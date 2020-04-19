
var pug = require('pug')

const os = require('os')
const config = require('../../config')
const extend = require('xtend')

module.exports = function (req, res) {
  var locals = {
    config: config.get(),
    nodeVersion: process.version,
    architecture: os.arch(),
    platform: os.type(),
    osRelease: os.release()
  }
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'application/json').send(JSON.stringify(locals))
  } else {
    locals = extend({
      section: 'admin',
      adminSection: 'status',
      user: req.user
    }, locals)
    res.send(pug.renderFile('templates/views/admin/status.jade', locals))
  }
}
