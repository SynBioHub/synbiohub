
const java = require('../java')
const extend = require('xtend')
const config = require('../config')

function convertToGenBank (inFilename, opts) {
  opts = extend({

    sbolFilename: inFilename,
    requireComplete: config.get('requireComplete'),
    requireCompliant: config.get('requireCompliant'),
    enforceBestPractices: config.get('requireBestPractice'),
    typesInURI: false,
    keepGoing: false,
    topLevelURI: ''

  }, opts)

  return java('convertToGenBank', opts).then((result) => {
    const { success, log, errorLog, resultFilename } = result

    return Promise.resolve(result)
  })
}

module.exports = convertToGenBank
