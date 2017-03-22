
const sparql = require('../../sparql/sparql')
const async = require('async')
const loadTemplate = require('../../loadTemplate')

function getComponentDefinitionMetadata(uri, graphUris) {

    return new Promise((resolve, reject) => {

        var templateParams = {
            componentDefinition: uri
        }

        var query = loadTemplate('sparql/getComponentDefinitionMetaData.sparql', templateParams)

        async.eachSeries(graphUris, (graphUri, next) => {

            graphUri = graphUri || config.get('triplestore').defaultGraph

            sparql.queryJson(query, graphUri).then((result) => {

                if(result && result[0]) {

                    resolve({
                        metaData: result[0],
                        graphUri: graphUri
                    })

                } else {

                    next()

                }

            })

        }, (err) => {

            if(err) {
                reject(err)
            } else {
                // not found
                resolve(null)
            }

        })

    })

}

module.exports = {
    getComponentDefinitionMetadata: getComponentDefinitionMetadata
}


