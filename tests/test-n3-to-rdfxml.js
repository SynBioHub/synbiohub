
fs = require('fs')

saveN3ToRdfXml = require('../lib/conversion/save-n3-to-rdfxml')

saveN3ToRdfXml([ fs.readFileSync('test2.n3') + '' ]).then((filename) => {
    console.log(filename)
})
