

const java = require('../lib/java')
const prepareSubmission = require('../lib/prepare-submission')

java.init().then(() => {

    return prepareSubmission(__dirname + '/cello.xml', {
        uriPrefix: 'http://synbiohub.org/user/jm/',
        newRootCollectionDisplayId: 'test_collection',
        ownedByURI: 'http://synbiohub.org/user/jm',
        creatorName: 'Fred Durst',
        name: 'Submitted genetic toggle switch',
        description: 'its a test',
        citationPubmedIDs: [],
        keywords: []
    })

}).then((result) => {

    const { success, log, errorLog, resultFilename } = result

    console.log(result)

})

