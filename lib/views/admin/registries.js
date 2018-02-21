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

    let federationData = {
        instanceUrl: config.get('instanceUrl'),
        uriPrefix: config.get('databasePrefix'),
        administratorEmail: config.get('administratorEmail'),
        updateEndpoint: 'updateWebOfRegistries',
        name: config.get('instanceUrl'),
        description: config.get('frontPageText')
    }

    let errors = [];

    if (federationData.instanceUrl === undefined) {
        errors.push('The instance URL is not set, this can be done from the admin dashboard.');
    }

    if (federationData.uriPrefix === undefined) {
        errors.push('The instance URI prefix is not set, this can be done from the admin dashboard.');
    }

    if (federationData.updateEndpoint === undefined) {
        errors.push('The update endpoint is not set, you likely need to upgrade your SynBioHub instance.');
    }

    if (federationData.name === undefined) {
        errors.push('The instance name is not set, this can be done from the admin dashboard.');
    }

    if (federationData.description === undefined) {
        errors.push('The instance description is not set, this can be done from the admin dashboard.');
    }

    Object.keys(configRegistries).forEach(key => {
        registries = registries.concat({
            uri: key,
            url: configRegistries[key]
        })
    })

    if (registered) {
        request.get(config.get('webOfRegistriesUrl') + "/instances/" + config.get('webOfRegistriesId') + '/', (err, resp, body) => {
            if (err || resp.statusCode >= 300) {
                config.delete('webOfRegistriesSecret');
                config.delete('webOfRegistriesId');
                unregistered(req, res, errors);
            } else {
                body = JSON.parse(body);

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
                    approved: body["approved"],
                    updateWorking: body["updateWorking"]
                }

                res.send(pug.renderFile('templates/views/admin/registries.jade', locals))
            }
        })
    } else {
        unregistered(req, res, errors);
    }
};

function unregistered(req, res, errors) {
    let registries = config.get('webOfRegistries');

    registries = Object.keys(registries).map(key => {
        return {
            uri: key,
            url: registries[key]
        }
    })

    var locals = {
        config: config.get(),
        section: 'admin',
        adminSection: 'registries',
        user: req.user,
        registries: registries,
        registered: false,
        wor: config.get('webOfRegistriesUrl'),
        worId: config.get('webOfRegistriesId'),
        errors: errors
    }

    res.send(pug.renderFile('templates/views/admin/registries.jade', locals))
}