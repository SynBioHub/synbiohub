
const saveN3ToRdfXml = require('../save-n3-to-rdfxml')
const java = require('../java')
const fs = require('mz/fs')

/* Takes an array of strings containing n3 triples
 * Returns the filename of a temporary XML file containing well-formatted SBOL.
 */
function n3ToSBOL(n3) {

    return saveN3ToRdfXml(n3).then((tempRdfFilename) => {

        java('rdfToSBOL', {

            sbolFilename: tempRdfFilename,
            uriPrefix: '',
            requireComplete: config.get('requireComplete'),
            requireCompliant: config.get('requireCompliant'),
            enforceBestPractices: config.get('requireBestPractice'),
            typesInURI: false,
            version: '1',
            keepGoing: false

        }).then((tempSbolFilename) => {

            return fs.unlink(tempRdfFilename).then(() => {
                return Promise.resolve(tempSbolFilename)
            })

        })
    })

}

module.exports = n3ToSBOL

