const config = require('../../config')
const request = require('request')

module.exports = function (req, res) {
    let data = {
        instanceUrl: config.get('instanceUrl'),
        uriPrefix: config.get('databasePrefix'),
        administratorEmail: config.get('administratorEmail'),
        updateEndpoint: 'updateWebOfRegistries',
        name: config.get('instanceUrl'),
        description: config.get('frontPageText')
    }

    request.post(req.body.webOfRegistries + "/instances/new/", { json: data }, (err, resp, body) => {
        if(err) {
            console.log("Federation error");
            console.log(err);
        }
        
        config.set('webOfRegistriesSecret', body["updateSecret"]);
        config.set('webOfRegistriesUrl', req.body.webOfRegistries);
        config.set('webOfRegistriesId', body["id"])

        res.redirect('/admin/registries')

    })
}