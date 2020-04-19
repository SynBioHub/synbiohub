
const config = require('../../config')

module.exports = function (req, res) {
  const id = req.body.id
  const category = req.body.category

  var plugins = config.get('plugins')

  if (!category || (category !== 'rendering' && category !== 'download' && category !== 'submit')) {
    return res.status(400).header('content-type', 'text/plain').send('Must provide a category with value of rendering, submit, or download')
  }
  if (!id) {
    return res.status(400).header('content-type', 'text/plain').send('Must provide a valid plugin id')
  }
  let index = parseInt(id, 10) - 1
  if (!plugins || !plugins[category] || !plugins[category][index]) {
    return res.status(400).header('content-type', 'text/plain').send('Invalid plugin id')
  }

  plugins[category].splice(index, 1)

  config.set('plugins', plugins)

  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'text/plain').send('Plugin (' + id + ', ' + category + ') deleted successfully')
  } else {
    res.redirect('/admin/plugins')
  }
}
