
var extend = require('xtend')
var config = require('../config')
var uriToUrl = require('../uriToUrl')


module.exports = function generateDataRecord(metadata) {

  return extend({

    "identifier": [
      metadata.name
    ],
    "mainEntity": {
      "@type": metadata.rdfType,
      "identifier": metadata.name,
      "url": config.get('instanceUrl').slice(0, -1) + uriToUrl(metadata.uri)
    },
    "url": config.get('instanceUrl').slice(0, -1) + uriToUrl(metadata.uri)

  }, config.get('bioschemas').DataRecord)

}
