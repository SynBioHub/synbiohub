
const java = require('../java')
const extend = require('xtend')
const config = require('../config')

const fs = require('mz/fs')

function prepareSnapshot(inFilename, opts) {

    console.log('***** CVO ' + inFilename)
    
    fs.writeFileSync('broken.xml', fs.readFileSync(inFilename) + '')

    opts = extend({

        sbolFilename: inFilename,
        requireComplete: config.get('requireComplete'),
        requireCompliant: config.get('requireCompliant'),
        enforceBestPractices: config.get('requireBestPractice'),
        typesInURI: false,
        version: '1',
        keepGoing: false,
        topLevelURI: ''

    }, opts)

    return java('prepareSnapshot', opts).then((result) => {

        const { success, log, errorLog, resultFilename } = result

        return Promise.resolve(result)
    })
}

module.exports = prepareSnapshot


