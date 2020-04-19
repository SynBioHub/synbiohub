
const pug = require('pug')

const config = require('../../config')

const extend = require('xtend')

module.exports = function (req, res) {
  const remotesConfig = config.get('remotes')

  const remotes = Object.keys(remotesConfig).map((id) => remotesConfig[id])

  var locals = {
    remotes: remotes,
    remoteTypes: ['ice', 'benchling']
  }
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'application/json').send(JSON.stringify(locals))
  } else {
    locals = extend({
      config: config.get(),
      section: 'admin',
      adminSection: 'registries',
      user: req.user
    }, locals)
    res.send(pug.renderFile('templates/views/admin/remotes.jade', locals))
  }
}
