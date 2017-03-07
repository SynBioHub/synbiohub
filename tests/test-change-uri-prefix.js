

const java = require('../lib/java')
const changeURIPrefix = require('../lib/change-uri-prefix')

java.init().then(() => {

    return changeURIPrefix(__dirname + '/cello.xml', {
        uriPrefix: 'http://synbiohub.org/user/jm/',
        version: '1'
    })

}).then((result) => {

    const { success, log, errorLog, resultFilename } = result

    console.log(result)

})

