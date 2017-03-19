

const SBOLDocument = require('sboljs')

const config = require('../../../config')

const n3ToSBOL = require('../../../conversion/n3-to-sbol')

const { fetchSBOLObjectRecursive } = require('./fetch-sbol-object-recursive')

const fs = require('mz/fs')

const sparql = require('../../../sparql/sparql')

const tmp = require('tmp-promise')

function fetchSBOLSource(remoteConfig, type, objectUri) {

    /* workaround libSBOLj not being able to understand prefixes and producing
     * invalid XML
     */
    const ns = {
        'xmlns:sbh': 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#'
    }

    return fetchSBOLObjectRecursive(remoteConfig, new SBOLDocument(), type, objectUri).then((res) => {

        return tmp.file().then((tempFile) => {

            return fs.writeFile(tempFile.path, res.sbol.serializeXML(ns))
                .then(() => Promise.resolve(tempFile.path))

        })

    })
}

module.exports = {
    fetchSBOLSource: fetchSBOLSource
}

