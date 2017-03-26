

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

        return tmp.file({ discardDescriptor: true }).then((tempFile) => {

            return fs.writeFile(tempFile.path, serializeSBOL(res.sbol))
                .then(() => Promise.resolve(tempFile.path))

        })

    })
}

module.exports = {
    fetchSBOLSource: fetchSBOLSource
}

