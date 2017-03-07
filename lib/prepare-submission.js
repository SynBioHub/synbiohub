
const java = require('./java')
const extend = require('xtend')
const config = require('./config')

function prepareSubmission(inFilename, opts) {

    opts = extend({

        sbolFilename: inFilename,
        uriPrefix: 'http://some_uri_prefix/',
        requireComplete: config.get('requireComplete'),
        requireCompliant: config.get('requireCompliant'),
        enforceBestPractices: config.get('requireBestPractice'),
        typesInURI: false,
        version: '1',
        keepGoing: false,
        topLevelURI: '',

        newRootCollectionDisplayId: '',
        ownedByURI: '',
        creatorName: '',
        name: '',
        description: '',
        citationPubmedIDs: [],
        keywords: [],

	webOfRegistries: config.get('webOfRegistries')
    }, opts)

    return java('prepareSubmission', opts).then((result) => {

        const { success, log, errorLog, resultFilename } = result

        return Promise.resolve(result)
    })
}

module.exports = prepareSubmission


