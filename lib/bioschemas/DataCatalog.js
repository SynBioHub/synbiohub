
var config = require('../config')
var extend = require('xtend')
var { getRootCollectionMetadata } = require('../query/collection')

module.exports = async function generateDataCatalog() {

  return extend({
    "name": config.get("instanceName"),
    "description": config.get("frontPageText"),
    "url": config.get("instanceUrl"),
    "keywords": config.get("keywords"),
    "identifier": [
        config.get('instanceName').replace(/\s+/g, '')
    ],
    "dataset": await generateDatasets()
  }, config.get('bioschemas').DataCatalog)

}

async function generateDatasets() {

  var collections = await getRootCollectionMetadata(null, null)

  return collections.map(generateDataset)
}

function generateDataset(collectionMetadata) {

  return extend({
    "name": collectionMetadata.name,
    "description": collectionMetadata.description,
    "url": collectionMetadata.url,
    "identifier": [
        collectionMetadata.displayId
    ],
    "distribution": collectionMetadata.url,
    "includedInDataCatalog": [
      config.get('instanceUrl')
    ],
  }, config.get('bioschemas').Dataset)
}
