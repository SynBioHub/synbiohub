const config = require('./config')
const logger = require('./logger')

function applyConsoleOverrides () {
  if (config.get('suppressErrorLogs') === true) {
    console.error = function () {}
  } else {
    console.error = logger.error.bind(logger)
  }

  if (config.get('suppressWarningLogs') === true) {
    console.warn = function () {}
  } else {
    console.warn = logger.warn.bind(logger)
  }

  if (config.get('suppressDebugLogs') === true) {
    console.debug = function () {}
  } else {
    console.debug = logger.debug.bind(logger)
  }

  if (config.get('suppressInfoLogs') === true) {
    console.log = function () {}
  } else {
    console.log = logger.info.bind(logger)
  }
}

module.exports = applyConsoleOverrides
