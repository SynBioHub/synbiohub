
const java = require('./java')
const extend = require('xtend')
const config = require('./config')

function cloneSubmission(inFilename, opts) {

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

	rootCollectionIdentity: '',
        originalCollectionDisplayId: '',
        originalCollectionVersion: '',
        newRootCollectionDisplayId: '',
	newRootCollectionVersion: '',
	webOfRegistries: config.get('webOfRegistries'),
	shareLinkSalt: config.get('shareLinkSalt'),

	overwrite_merge: ''

    }, opts)

    console.log(opts)

    return java('cloneSubmission', opts).then((result) => {

        const { success, log, errorLog, resultFilename } = result

        return Promise.resolve(result)
    })
}

module.exports = cloneSubmission


