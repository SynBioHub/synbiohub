
var async = require('async')

function getComponentDefinition(prefix, uri, stores, callback) {

    async.eachSeries(stores, (store, next) => {

        store.getComponentSBOL(prefix, uri, (err, sbol, object) => {

            if(sbol) {

                sbol.store = store
                object.store = store

                callback(null, sbol, object)

            } else {

                next()

            }
        })

    }, (err) => {

        callback(err || new Error('component definition not found'))
                    
    })

}

function getComponentDefinitionMetadata(prefix, uri, stores, callback) {

    async.eachSeries(stores, (store, next) => {

        store.getComponentMetadata(prefix, uri, (err, metadata) => {

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

function getCollection(prefix, uri, stores, callback) {

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

function getType(uri, stores, callback) {

    async.eachSeries(stores, (store, next) => {

	store.sparql('SELECT ?type WHERE { <' + uri + '> a ?type }', (err, result) => {

            if(result[0]) {
		
		console.log('gt='+result)
                callback(null, result)

            } else {

		next()

            }
        })

    }, (err) => {

        callback(err || new Error('collection not found'))
                    
    })

}

module.exports = {
    getComponentDefinition: getComponentDefinition,
    getComponentDefinitionMetadata: getComponentDefinitionMetadata,
    getCollection: getCollection,
    getType: getType
}






