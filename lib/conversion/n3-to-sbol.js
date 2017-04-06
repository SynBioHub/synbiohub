
const saveN3ToRdfXml = require('./save-n3-to-rdfxml')
const java = require('../java')
const fs = require('mz/fs')
const config = require('../config')


/* Takes an array of strings containing n3 triples
 * Returns the filename of a temporary XML file containing well-formatted SBOL.
 */
function n3ToSBOL(n3) {

    return saveN3ToRdfXml(n3).then((tempRdfFilename) => {

        return java('rdfToSBOL', {

            sbolFilename: tempRdfFilename,
            uriPrefix: '',
            requireComplete: config.get('requireComplete'),
            requireCompliant: config.get('requireCompliant'),
            enforceBestPractices: config.get('requireBestPractice'),
            typesInURI: false,
            version: '',
            keepGoing: true

        }).then((res) => {

            const { success, log, errorLog, resultFilename } = res

            if(!success) {
		return Promise.reject(new Error(errorLog))
            }

            console.log('tsf is ')
            console.log(resultFilename)

            return fs.unlink(tempRdfFilename).then(() => {
                return Promise.resolve(resultFilename)
            })

        })
    })

}

module.exports = n3ToSBOL

