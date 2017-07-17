const config = require('../../config')
const request = require('request')

module.exports = function (req, res) {
    let data = {
        instanceUrl: config.get('instanceUrl'),
        uriPrefix: config.get('databasePrefix'),
        administratorEmail: req.body.administratorEmail,
        updateEndpoint: 'updateWebOfRegistries'
    }

    request.post(req.body.webOfRegistries + "/instances/new/", { json: data }, (err, resp, body) => {

        config.set('webOfRegistriesSecret', body["updateSecret"]);

        res.redirect('/admin/registries')

    })
}