
const sparql = require('../../sparql/sparql')
const async = require('async')
const loadTemplate = require('../../loadTemplate')
const config = require('../../config')

function getComponentDefinitionMetadata (uri, graphUri) {
  var templateParams = {
    componentDefinition: uri
  }

  var query = loadTemplate('sparql/getComponentDefinitionMetaData.sparql', templateParams)

  graphUri = graphUri || config.get('triplestore').defaultGraph

  return sparql.queryJson(query, graphUri).then((result) => {
    if (result && result[0]) {
      return Promise.resolve({
        metaData: result[0],
        graphUri: graphUri
      })
    } else {
      return Promise.resolve(null)
    }
  })
}

module.exports = {
  getComponentDefinitionMetadata: getComponentDefinitionMetadata
}
