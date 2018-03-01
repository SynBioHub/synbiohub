
const java = require('./java')

function buildCombineArchive(sbolFilename, creatorInfo) {
    let options = {
        sbolFilename: sbolFilename,
        creatorInfo: creatorInfo
    }

    return java('buildCombineArchive', options).then(result => {

        const { success, log, errorLog, resultFilename } = result;

        return Promise.resolve(result);
    })
}

module.exports = buildCombineArchive 


