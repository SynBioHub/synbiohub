const config = require('../config');

module.exports = function(req, res) {
    let registries = req.body;
    let current = config.get('webOfRegistries')

    registries.forEach(registry => {
        current[registry.uriPrefix] = registry.instanceUrl;
    })

    config.set('webOfRegistries', current);

    res.send(200);
}