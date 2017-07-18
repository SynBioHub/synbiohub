const pug = require('pug')

const sparql = require('../../sparql/sparql')

const db = require('../../db')

const config = require('../../config')

const request = require('request')

module.exports = function (req, res) {

    var registries = []

    var configRegistries = config.get('webOfRegistries')

    let registered = true;

    if (config.get('webOfRegistriesSecret') === undefined) {
        registered = false;
    }

    Object.keys(configRegistries).forEach(key => {
        registries = registries.concat({
            uri: key,
            url: configRegistries[key]
        })
    })

    if (registered) {
        request.get(config.get('webOfRegistriesUrl') + "/instances/" + config.get('webOfRegistriesId') + '/', (err, resp, body) => {
            body = JSON.parse(body);
            console.log("eMAIL: " + body.administratorEmail)

            var locals = {
                config: config.get(),
                section: 'admin',
                adminSection: 'registries',
                user: req.user,
                registries: registries,
                registered: registered,
                secret: config.get('webOfRegistriesSecret'),
                wor: config.get('webOfRegistriesUrl'),
                worId: config.get('webOfRegistriesId'),
                adminEmail: body["administratorEmail"],
                approved: body["approved"]
            }

            res.send(pug.renderFile('templates/views/admin/registries.jade', locals))
        })
    } else {
        var locals = {
            config: config.get(),
            section: 'admin',
            adminSection: 'registries',
            user: req.user,
            registries: registries,
            registered: registered,
            secret: config.get('webOfRegistriesSecret'),
            wor: config.get('webOfRegistriesUrl'),
            worId: config.get('webOfRegistriesId')
        }

        res.send(pug.renderFile('templates/views/admin/registries.jade', locals))
    }


};