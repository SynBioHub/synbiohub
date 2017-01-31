
var async = require('async')

var loadTemplate = require('./loadTemplate')

const sparql = require('./sparql/sparql')

const getSBOL = require('./getSBOL')

const SBOLDocument = require('sboljs')

function getComponentDefinition(uri, graphUris, callback) {

    async.eachSeries(graphUris, (graphUri, next) => {

        getSBOL(new SBOLDocument(), 'ComponentDefinition', graphUri, [ uri ], (err, sbol, object) => {

            if(sbol) {

                sbol.graphUri = graphUri
                object.graphUri = graphUri

                callback(null, sbol, object)

            } else {

                next()

            }
        })

    }, (err) => {

        callback(err || new Error('component definition not found'))
                    
    })

}

function getModuleDefinition(uri, graphUris, callback) {

    async.eachSeries(graphUris, (graphUri, next) => {

        getSBOL(new SBOLDocument(), 'ModuleDefinition', graphUri, [ uri ], (err, sbol, object) => {

            if(sbol) {

                sbol.graphUri = graphUri
                object.graphUri = graphUri

                callback(null, sbol, object)

            } else {

                next()

            }
        })

    }, (err) => {

        callback(err || new Error('module definition not found'))
                    
    })

}

function getSequence(uri, stores, callback) {

    async.eachSeries(stores, (store, next) => {

        store.getSequenceSBOL(prefix, uri, (err, sbol, object) => {

            if(sbol) {

                sbol.store = store
                object.store = store

                callback(null, sbol, object)

            } else {

                next()

            }
        })

    }, (err) => {

        callback(err || new Error('sequence not found'))
                    
    })

}

function getModel(uri, stores, callback) {

    async.eachSeries(stores, (store, next) => {

        store.getModelSBOL(prefix, uri, (err, sbol, object) => {

            if(sbol) {

                sbol.store = store
                object.store = store

                callback(null, sbol, object)

            } else {

                next()

            }
        })

    }, (err) => {

        callback(err || new Error('model not found'))
                    
    })

}

function getGenericTopLevel(uri, stores, callback) {

    async.eachSeries(stores, (store, next) => {

        store.getGenericTopLevelSBOL(prefix, uri, (err, sbol, object) => {

            if(sbol) {

                sbol.store = store
                object.store = store

                callback(null, sbol, object)

            } else {

                next()

            }
        })

    }, (err) => {

        callback(err || new Error('genericTopLevel not found'))
                    
    })

}

function getModuleDefinitionMetadata(uri, stores, callback) {

    async.eachSeries(stores, (store, next) => {

        store.getModuleDefinitionMetadata(prefix, uri, (err, metadata) => {

            if(metadata) {

                metadata.store = store

                callback(null, metadata)

            } else {

                next()
            }

        })

    }, (err) => {

        callback(err || new Error('module definition metadata not found'))
                    
    })

}

function getComponentDefinitionMetadata(uri, stores, callback) {

    async.eachSeries(stores, (store, next) => {

        store.getComponentDefinitionMetadata(prefix, uri, (err, metadata) => {

            if(metadata) {

                metadata.store = store

                callback(null, metadata)

            } else {

                next()
            }

        })

    }, (err) => {

        callback(err || new Error('component definition metadata not found'))
                    
    })

}

function getCollection(uri, stores, callback) {

    async.eachSeries(stores, (store, next) => {

        store.getCollectionSBOL(prefix, uri, (err, sbol, object) => {

            if(sbol) {

                sbol.store = store
                object.store = store

                callback(null, sbol, object)

            } else {

                next()

            }
        })

    }, (err) => {

        callback(err || new Error('collection not found'))
                    
    })

}

function getCollectionMetaData(uri, stores, callback) {

    var templateParams = {
        collection: uri
    }

    var query = loadTemplate('sparql/getCollectionMetaData.sparql', templateParams)

    async.eachSeries(stores, (store, next) => {

	store.sparql(query, (err, result) => {

            if(result && result[0]) {

                callback(null, result, store.storeUrl)

            } else {

		next()

            }
        })

    }, (err) => {

        callback(err || new Error('collection not found'))
                    
    })

}

function getCollectionMembers(uri, stores, callback) {

    var templateParams = {
        collection: uri
    }

    var query = loadTemplate('sparql/getCollectionMembers.sparql', templateParams)

    async.eachSeries(stores, (store, next) => {

	store.sparql(query, (err, result) => {

            if(result && result[0]) {

                callback(null, result)

            } else {

		next()

            }
        })

    }, (err) => {

        callback(err || new Error('collection not found'))
                    
    })

}


function getType(uri, stores, callback) {

    async.eachSeries(stores, (store, next) => {

        sparql.queryJson('SELECT ?type WHERE { <' + uri + '> a ?type }', store, (err, result) => {

            if(result && result[0]) {

                callback(null, result)

            } else {

                next()

            }

        })

    }, (err) => {

        callback(err || new Error('top level not found'))
                    
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
    getType: getType
}






