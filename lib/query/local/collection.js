
const loadTemplate = require('../../loadTemplate')
const sparql = require('../../sparql/sparql')
const config = require('../../config')

function getCollectionMemberCount(uri, graphUri) {

    var query = loadTemplate('./sparql/CountMembers.sparql', {
        collection: uri
    })

    return sparql.queryJson(query, graphUri).then((result) => {

        if(result && result[0]) {

            return Promise.resolve(result[0].count)

        } else {

            return Promise.reject('collection not found')

        }

    })

}

function getRootCollectionMetadata(graphUri) {

    var query = loadTemplate('./sparql/RootCollectionMetadata.sparql', { });

    return sparql.queryJson(query, graphUri).then((sparqlResults) => {

       return Promise.resolve(
           sparqlResults.map(function(result) {
            return {
                uri: result['Collection'],
                name: result['name'] || '',
                description: result['description'] || '',
                displayId: result['displayId'] || '',
                version: result['version'] || ''
            };
        }))
    })
}

function getContainingCollections(uri, graphUri, reqUrl) {

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
                name: result.title?result.title:result.displayId
            }
        }))

    })

}

function getCollectionMembers(uri, graphUri, limit, offset) {

    var templateParams = {
        collection: uri,
        offset: offset !== undefined ? ' OFFSET ' + offset : '',
        limit: limit !== undefined ? ' LIMIT ' + limit : ''
    }

    var query = loadTemplate('sparql/getCollectionMembers.sparql', templateParams)

    return sparql.queryJson(query, graphUri).then((result) => {

        if(result && result[0]) {

            return Promise.resolve(result)

        } else {

            return Promise.reject('collection not found')

        }

    })

}

function getCollectionMetaData(uri, graphUri) {

    var templateParams = {
        collection: uri
    }

    var query = loadTemplate('sparql/getCollectionMetaData.sparql', templateParams)

    return sparql.queryJson(query, graphUri).then((result) => {

        if(result && result[0]) {

            return Promise.resolve(result[0])

        } else {

            return Promise.resolve(null) /* not found */

        }

    })

}

module.exports = {
    getRootCollectionMetadata: getRootCollectionMetadata,
    getCollectionMetaData: getCollectionMetaData,
    getCollectionMemberCount: getCollectionMemberCount,
    getContainingCollections: getContainingCollections,
    getCollectionMembers: getCollectionMembers
}

