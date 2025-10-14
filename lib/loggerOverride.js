const config = require('./config')
const logger = require('./logger')

function applyConsoleOverrides () {
  if (config.get('suppressLogs') === true) {
    console.log = function () {}
    console.debug = function () {}
    console.warn = function () {}
  } else {
    console.log = logger.info.bind(logger)
    console.debug = logger.debug.bind(logger)
    console.warn = logger.warn.bind(logger)
  }
  console.error = logger.error.bind(logger)
}

module.exports = applyConsoleOverrides
