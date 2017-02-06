
var async = require('async')

var loadTemplate = require('./loadTemplate')

const sparql = require('./sparql/sparql')

const getSBOL = require('./getSBOL')

const SBOLDocument = require('sboljs')

const config = require('./config')

function getComponentDefinition(uri, graphUris) {

    return new Promise((resolve, reject) => {

        async.eachSeries(graphUris, (graphUri, next) => {

            graphUri = graphUri || config.get('triplestore').defaultGraph

            getSBOL(new SBOLDocument(), 'ComponentDefinition', graphUri, [ uri ]).then((res) => {

                if(res && res.object) {

                    res.sbol.graphUri = graphUri
                    res.object.graphUri = graphUri

                    resolve({
                        graphUri: graphUri,
                        sbol: res.sbol,
                        object: res.object
                    })

                } else {

                    next()

                }
                
            })

        }, () => {

            reject(new Error('getComponentDefinition: not found'))

        })

    })

}

function getModuleDefinition(uri, graphUris) {

    return new Promise((resolve, reject) => {

        async.eachSeries(graphUris, (graphUri, next) => {

            getSBOL(new SBOLDocument(), 'ModuleDefinition', graphUri, [ uri ]).then((res) => {

            graphUri = graphUri || config.get('triplestore').defaultGraph

                if(res && res.object) {

                    res.sbol.graphUri = graphUri
                    res.object.graphUri = graphUri

                    resolve({
                        graphUri: graphUri,
                        sbol: res.sbol,
                        object: res.object
                    })

                } else {

                    next()

                }
                
            })

        }, () => {

            reject(new Error('getModuleDefinition: not found'))

        })

    })

}

function getSequence(uri, graphUris) {

    return new Promise((resolve, reject) => {

        async.eachSeries(graphUris, (graphUri, next) => {

            graphUri = graphUri || config.get('triplestore').defaultGraph

            getSBOL(new SBOLDocument(), 'Sequence', graphUri, [ uri ]).then((res) => {

                if(res && res.object) {

                    res.sbol.graphUri = graphUri
                    res.object.graphUri = graphUri

                    resolve({
                        graphUri: graphUri,
                        sbol: res.sbol,
                        object: res.object
                    })

                } else {

                    next()

                }
                
            })

        }, () => {

            reject(new Error('getSequence: not found'))

        })

    })

}

function getModel(uri, graphUris) {

    return new Promise((resolve, reject) => {

        async.eachSeries(graphUris, (graphUri, next) => {

            graphUri = graphUri || config.get('triplestore').defaultGraph

            getSBOL(new SBOLDocument(), 'Model', graphUri, [ uri ]).then((res) => {

                if(res && res.object) {

                    res.sbol.graphUri = graphUri
                    res.object.graphUri = graphUri

                    resolve({
                        graphUri: graphUri,
                        sbol: res.sbol,
                        object: res.object
                    })

                } else {

                    next()

                }
                
            })

        }, () => {

            reject(new Error('getModel: not found'))

        })

    })

}

function getGenericTopLevel(uri, graphUris) {

    return new Promise((resolve, reject) => {

        async.eachSeries(graphUris, (graphUri, next) => {

            graphUri = graphUri || config.get('triplestore').defaultGraph

            getSBOL(new SBOLDocument(), 'GenericTopLevel', graphUri, [ uri ]).then((res) => {

                if(res && res.object) {

                    res.sbol.graphUri = graphUri
                    res.object.graphUri = graphUri

                    resolve({
                        graphUri: graphUri,
                        sbol: res.sbol,
                        object: res.object
                    })

                } else {

                    next()

                }
                
            })

        }, () => {

            reject(new Error('getGenericTopLevel: not found'))

        })

    })

}

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


function getCollection(uri, graphUris) {

    return new Promise((resolve, reject) => {

        async.eachSeries(graphUris, (graphUri, next) => {

            graphUri = graphUri || config.get('triplestore').defaultGraph

            getSBOL(new SBOLDocument(), 'Collection', graphUri, [ uri ]).then((res) => {

                if(res && res.object) {

                    res.sbol.graphUri = graphUri
                    res.object.graphUri = graphUri

                    resolve({
                        graphUri: graphUri,
                        sbol: res.sbol,
                        object: res.object
                    })

                } else {

                    next()

                }
                
            })

        }, () => {

            reject(new Error('getCollection: not found'))

        })

    })

}

function getCollectionMetaData(uri, graphUris) {

    return new Promise((resolve, reject) => {

        var templateParams = {
            collection: uri
        }

        var query = loadTemplate('sparql/getCollectionMetaData.sparql', templateParams)

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

function getCollectionMembers(uri, graphUris) {

    return new Promise((resolve, reject) => {

        var templateParams = {
            collection: uri
        }

        var query = loadTemplate('sparql/getCollectionMembers.sparql', templateParams)

        async.eachSeries(graphUris, (graphUri, next) => {

            graphUri = graphUri || config.get('triplestore').defaultGraph

            sparql.queryJson(query, graphUri).then((result) => {

                if(result && result[0]) {

                    resolve(result)

                } else {

                    next()

                }

            })

        }, (err) => {

            reject(err || new Error('collection not found'))

        })

    })

}


function getType(uri, stores) {

    return new Promise((resolve, reject) => {

        async.eachSeries(stores, (store, next) => {

            sparql.queryJson('SELECT ?type WHERE { <' + uri + '> a ?type }', store).then((result) => {

                if(result && result[0]) {

                    resolve(result)

                } else {

                    next()

                }

            })

        }, (err) => {

            reject(new Error('top level not found'))
                        
        })

    })

}

function getContainingCollections(uri, graphUri) {

    var query =
        'PREFIX sbol2: <http://sbols.org/v2#>\n' +
        'PREFIX dcterms: <http://purl.org/dc/terms/>\n' +
    'SELECT ?subject ?displayId ?title WHERE {' +
        '   ?subject a sbol2:Collection .' +
        '   ?subject sbol2:member <' + uri + '> .' +
        '   OPTIONAL { ?subject sbol2:displayId ?displayId } .' +
        '   OPTIONAL { ?subject dcterms:title ?title } .' +
        '}'

    return sparql.queryJson(query, graphUri).then((results) => {

        return Promise.resolve(results.map((result) => {
            return {
                uri: result.subject,
                url: '/'+result.subject.toString().replace(config.get('databasePrefix'),''),
                name: result.title?result.title:result.displayId
            }
        }))

    })

}

module.exports = {
    getModuleDefinition: getModuleDefinition,
    getModuleDefinitionMetadata: getModuleDefinitionMetadata,
    getComponentDefinition: getComponentDefinition,
    getComponentDefinitionMetadata: getComponentDefinitionMetadata,
    getSequence: getSequence,
    getModel: getModel,
    getGenericTopLevel: getGenericTopLevel,
    getCollection: getCollection,
    getCollectionMetaData: getCollectionMetaData,
    getCollectionMembers: getCollectionMembers,
    getContainingCollections: getContainingCollections,
    getType: getType
}






