
const pug = require('pug')

const sparql = require('../../sparql/sparql')

const db = require('../../db')

const config = require('../../config')

module.exports = function(req, res) {

    var registries = []

    var configRegistries = config.get('webOfRegistries')

    Object.keys(configRegistries).forEach(key => {
        registries = registries.concat({
            uri: key,
            url: configRegistries[key]
        })
    })

    var locals = {
        config: config.get(),
        section: 'admin',
        adminSection: 'registries',
        user: req.user,
        registries: registries,
    }

    res.send(pug.renderFile('templates/views/admin/registries.jade', locals))
};
