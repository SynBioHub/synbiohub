
const config = require('../../config')

module.exports = function(req, res) {
    const name = req.body.name
    const id = req.body.id
    const category = req.body.category

    let url = req.body.url

    if(!url.endsWith('/')) {
        url = url + '/'
    }

    var plugins = config.get('plugins')

    if(id == "New") {
        plugins[category].push({
            name: name,
            url: url,
        })
    } else {
        let index = parseInt(id, 10) - 1
        plugins[category][index] = {
            name: name,
            url: url,
        }
    }

    console.log(plugins)
    config.set('plugins', plugins)
    res.redirect('/admin/plugins')
}
