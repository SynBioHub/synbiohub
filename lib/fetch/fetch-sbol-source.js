
const config = require('../config')
const splitUri = require('../splitUri')

const remote = {
    synbiohub: require('./remote/synbiohub/fetch-sbol-source'),
    ice: require('./remote/ice/fetch-sbol-source')
}

function fetchSBOLSource(type, uri, graphUri) {

    const args = [].slice.call(arguments, 0)

    /* fetchSBOLSource(uri, graphUri)
     */
    if(args.length === 2) {

        type = null
        uri = args[0]
        graphUri = args[1]

    }

    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined ?
                remote[remoteConfig.type].fetchSBOLSource(remoteConfig, type, uri) :
                local.fetchSBOLSource(type, uri, graphUri)

}

module.exports = {
    fetchSBOLSource: fetchSBOLSource
}

