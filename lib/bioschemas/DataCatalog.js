
var config = require('../config')
var extend = require('xtend')
var { getRootCollectionMetadata } = require('../query/collection')
var generateDataset = require('./Dataset')
var striptags = require('striptags')

module.exports = async function generateDataCatalog () {
  return extend({
    'name': config.get('instanceName'),
    'description': striptags(config.get('frontPageText')),
    'url': config.get('instanceUrl'),
    'keywords': config.get('keywords'),
    'identifier': [
      config.get('instanceName').replace(/\s+/g, '')
    ],
    'dataset': await generateDatasets()
  }, config.get('bioschemas').DataCatalog)
}

async function generateDatasets () {
  var collections = await getRootCollectionMetadata(null, null)

  return collections.map(generateDataset)
}
