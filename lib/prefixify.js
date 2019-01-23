
const assert = require('assert')

const config = require('./config')

const prefixes = config.get('namespaces')

const prefixNames = Object.keys(prefixes)

function prefixify (uri) {
  for (var i = 0; i < prefixNames.length; ++i) {
    var prefixName = prefixNames[i]
    var prefixUri = prefixes[prefixName]

    if (uri.indexOf(prefixUri) === 0) {
      return {
        prefix: prefixName.split(':')[1],
        uri: uri.slice(prefixUri.length)
      }
    }
  }

  return {
    prefix: '',
    uri: uri
  }
}

module.exports = prefixify
