
const config = require('../../config')

module.exports = function(req, res) {
    const id = parseInt(req.body.id, 10) - 1
    const name = req.body.name
    const url = req.body.url
    const category = req.body.category

    var plugins = config.get('plugins')

    plugins[category].splice(id, 1)

    config.set('plugins', plugins)
    res.redirect('/admin/plugins')
}
