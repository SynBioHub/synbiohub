
const pug = require('pug')
const config = require('../../config')

module.exports = function (req, res) {
  return form(req, res)
}

function form (req, res) {
  let plugins = config.get('plugins')

  plugins.rendering = plugins.rendering.map((plugin, idx) => {
    plugin.index = idx
    return plugin
  })

  plugins.download = plugins.download.map((plugin, idx) => {
    plugin.index = idx
    return plugin
  })

  const locals = {
    config: config.get(),
    section: 'admin',
    adminSection: 'plugins',
    user: req.user,
    plugins: plugins
  }

  res.send(pug.renderFile('templates/views/admin/plugins.jade', locals))
}
