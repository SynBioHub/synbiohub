
const config = require('../config')

const local = require('./local/type')

const remote = {
    synbiohub: require('./remote/synbiohub/type'),
    ice: require('./remote/ice/type')
}

const splitUri = require('../splitUri')

function getType(uri, graphUri) {

    const { submissionId } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined ?
                remote[remoteConfig.type].getType(remoteConfig, uri) :
                local.getType(uri, graphUri)
}

module.exports = {
    getType: getType
}

