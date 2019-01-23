
var config = require('../config')
var extend = require('xtend')
var uriToUrl = require('../uriToUrl')

module.exports = function generateDataset (collectionMetadata) {
  return extend({
    'name': collectionMetadata.name,
    'description': collectionMetadata.description,
    'url': config.get('instanceUrl').slice(0, -1) + uriToUrl(collectionMetadata.colUrl),
    'identifier': [
      collectionMetadata.displayId
    ],
    'distribution': collectionMetadata.url,
    'includedInDataCatalog': [
      config.get('instanceUrl')
    ]
  }, config.get('bioschemas').Dataset)
}
