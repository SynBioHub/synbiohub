
const { fetchSBOLObjectRecursive } = require('./fetch-sbol-object-recursive')
const SBOLDocument = require('sboljs')

const tmp = require('tmp-promise')
const fs = require('mz/fs')

function fetchSBOLSource(remoteConfig, type, objectUri) {

    return fetchSBOLObjectRecursive(remoteConfig, new SBOLDocument(), type, objectUri).then((res) => {
        
        return tmp.file().then((tempFile) => {

            return fs.writeFile(tempFile.path, res.sbol.serializeXML())
                .then(() => Promise.resolve(tempFile.path))

        })

    })

}

module.exports = {
    fetchSBOLSource: fetchSBOLSource
}


