
var SBOLDocument = require('sboljs')

var assert = require('assert')

const config = require('../config')
const splitUri = require('../splitUri')

const local = require('./local/fetch-sbol-object-non-recursive')

const remote = {
    synbiohub: require('./remote/synbiohub/fetch-sbol-object-recursive'),
    ice: require('./remote/ice/fetch-sbol-object-recursive'),
    benchling: require('./remote/benchling/fetch-sbol-object-recursive')
}

function fetchSBOLObjectNonRecursive(sbol, type, uri, graphUri) {

    const args = [].slice.call(arguments, 0)

    /* fetchSBOLObjectRecursive(uri, graphUri)
     */
    if(args.length === 2) {

        sbol = new SBOLDocument()
        type = null
        uri = args[0]
        graphUri = args[1]

    } else if(args.length === 3) {

        /* fetchSBOLObjectRecursive(type, uri, graphUri)
        */
        sbol = new SBOLDocument()
        type = args[0]
        uri = args[1]
        graphUri = args[2]
    }

    if(Array.isArray(uri)) {
        assert(uri.length === 1)
        uri = uri[0]
    }

    const { submissionId, version } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined && version === 'current' ?
                remote[remoteConfig.type].fetchSBOLObjectRecursive(remoteConfig, sbol, type, uri) :
                local.fetchSBOLObjectNonRecursive(sbol, type, uri, graphUri)

}

module.exports = {
    fetchSBOLObjectNonRecursive: fetchSBOLObjectNonRecursive
}

