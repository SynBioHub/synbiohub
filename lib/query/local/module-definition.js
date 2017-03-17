
function getModuleDefinitionMetadata(uri, graphUris) {

    return new Promise((resolve, reject) => {

        var templateParams = {
            moduleDefinition: uri
        }

        var query = loadTemplate('sparql/getModuleDefinitionMetaData.sparql', templateParams)

        async.eachSeries(graphUris, (graphUri, next) => {

            graphUri = graphUri || config.get('triplestore').defaultGraph

            sparql.queryJson(query, graphUri).then((result) => {

                if(result && result[0]) {

                    resolve({
                        graphUri: graphUri,
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

