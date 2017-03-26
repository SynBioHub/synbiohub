

const SBOLDocument = require('sboljs')

const config = require('../../../config')

const n3ToSBOL = require('../../../conversion/n3-to-sbol')

const { fetchSBOLObjectRecursive } = require('./fetch-sbol-object-recursive')

const fs = require('mz/fs')

const sparql = require('../../../sparql/sparql')

const tmp = require('tmp-promise')

const serializeSBOL = require('../../../serializeSBOL')

function fetchSBOLSource(remoteConfig, type, objectUri) {

    return fetchSBOLObjectRecursive(remoteConfig, new SBOLDocument(), type, objectUri).then((res) => {

        return tmp.tmpName().then((tmpFilename) => {

            return fs.writeFile(tmpFilename, serializeSBOL(res.sbol))
                .then(() => Promise.resolve(tmpFilename))

        })

    })
}

module.exports = {
    fetchSBOLSource: fetchSBOLSource
}

