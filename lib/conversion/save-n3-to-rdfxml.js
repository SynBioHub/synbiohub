

const tmp = require('tmp')
const spawn = require('child_process').spawn


/* Takes an array of strings containing n3 triples
 * Returns the filename of a temporary RDF+XML file containing the collated RDF.
 *
 * TODO handle errors
 */
function saveN3ToRdfXml(n3) {

    return new Promise((resolve, reject) => {

        const childProcess = spawn(__dirname + '/../../scripts/n3_to_rdfxml.sh', [], {
            cwd: __dirname + '/../../scripts',
            env: {
                NODE: process.execPath
            }
        })

        childProcess.stderr.on('data', (data) => {

            console.log('[n3_to_rdfxml.sh]', data.toString())

        })

        childProcess.stdout.on('data', (data) => {

            const filename = data.toString()

            console.log('Temp rdf+xml file received from script: ' + filename)

            resolve(filename)

        })

        writeNext()

        function writeNext() {

            childProcess.stdin.write(n3[0], () => {

                n3.splice(0, 1)

                if(n3.length > 0) {
                    writeNext()
                } else {
                    childProcess.stdin.end()
                }

            })
        }
    })

}


module.exports = saveN3ToRdfXml


