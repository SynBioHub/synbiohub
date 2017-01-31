
var loadTemplate = require('../loadTemplate')

var config = require('../config')

var federate = require('../federation/federate')
var collateObjects = require('../federation/collateObjects')

function PrefixesEndpoint(req, callback) {

    console.log('endpoint: PrefixesEndpoint')

    callback(null, 200, {
        mimeType: 'application/json',
        body: JSON.stringify(config.get('prefixes'))
    })

}

module.exports = federate(PrefixesEndpoint, collateObjects)


