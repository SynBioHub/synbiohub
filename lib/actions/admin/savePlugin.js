
const config = require('../../config')

module.exports = function (req, res) {
  const name = req.body.name
  const id = req.body.id
  const category = req.body.category

  let url = req.body.url

  var plugins = config.get('plugins')

  if (!category || (category !== 'rendering' && category !== 'download' && category !== 'submit')) {
    return res.status(400).header('content-type', 'text/plain').send('Must provide a category with value of rendering, submit, or download')
  }
  if (!name) {
    return res.status(400).header('content-type', 'text/plain').send('Must provide a valid plugin name')
  }
  if (!id) {
    return res.status(400).header('content-type', 'text/plain').send('Must provide a valid plugin id')
  } else if (id !== 'New') {
    let index = parseInt(id, 10) - 1
    if (!plugins || !plugins[category]) {
      return res.status(500).header('content-type', 'text/plain').send('No plugins found')
    }
    if (!plugins[category][index]) {
      return res.status(404).header('content-type', 'text/plain').send('Plugin not found')
    }
  }
  if (!url) {
    return res.status(400).header('content-type', 'text/plain').send('Must provide a valid plugin URL')
  }

  if (!url.endsWith('/')) {
    url = url + '/'
  }

  if (id === 'New') {
    plugins[category].push({
      name: name,
      url: url,
      index: plugins[category].length
    })
  } else {
    let index = parseInt(id, 10) - 1
    plugins[category][index] = {
      name: name,
      url: url,
      index: index
    }
  }

  config.set('plugins', plugins)
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'text/plain').send('Plugin (' + id + ', ' + name + ', ' + url + ', ' + category + ') saved successfully')
  } else {
    res.redirect('/admin/plugins')
  }
}
