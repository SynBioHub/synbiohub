
const java = require('./java')
const extend = require('xtend')
const config = require('./config')

function prepareSubmission(inFilename, opts) {

    opts = extend({
        sbolFilename: inFilename,
        databasePrefix: config.get('databasePrefix'),
        uriPrefix: 'http://some_uri_prefix/',
        requireComplete: config.get('requireComplete'),
        requireCompliant: config.get('requireCompliant'),
        enforceBestPractices: config.get('requireBestPractice'),
        typesInURI: false,
        version: '1',
        keepGoing: true,
        topLevelURI: '',
        submit: false,
        copy: false,
        rootCollectionIdentity: '',
        newRootCollectionDisplayId: '',
        newRootCollectionVersion: '',
        ownedByURI: '',
        creatorName: '',
        name: '',
        description: '',
        citationPubmedIDs: [],
        collectionChoices: [],
        overwrite_merge: '',
        webOfRegistries: config.get('webOfRegistries'),
        shareLinkSalt: config.get('shareLinkSalt')
    }, opts)

    return java('prepareSubmission', opts).then((result) => {

        const { success, log, errorLog, resultFilename } = result

        return Promise.resolve(result)
    })
}

module.exports = prepareSubmission


