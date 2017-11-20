
const config = require('./config')

function splitUri(uri) {

    const afterPrefix = uri.slice(config.get('databasePrefix').length)

    const fragments = afterPrefix.split('/')

    return {
    	prefix: config.get('databasePrefix'),
        scope: fragments[0],
        submissionId: fragments[1],
        displayId: fragments[2],
        version: fragments[3]
    }
}

module.exports = splitUri


