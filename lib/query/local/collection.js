
const loadTemplate = require('../../loadTemplate')
const sparql = require('../../sparql/sparql')
const config = require('../../config')
var escape = require('pg-escape')

function getCollectionMemberCount(uri, graphUri, search) {

    var query = loadTemplate('./sparql/CountMembers.sparql', {
        collection: uri,
        search: search !== '' && search !== undefined ? escape(
				'FILTER(CONTAINS(lcase(?displayId), lcase(%L))||CONTAINS(lcase(?name), lcase(%L))||CONTAINS(lcase(?description), lcase(%L)))',
				search, search, search
			) : ''
    })
    console.log(query)

    return sparql.queryJson(query, graphUri).then((result) => {

        if (result && result[0]) {
            console.log(result)

            return Promise.resolve(result[0].count)

        } else {

            return Promise.reject('collection not found')

        }

    })

}

function getRootCollectionMetadata(graphUri) {

    var query = loadTemplate('./sparql/RootCollectionMetadata.sparql', {});

    return sparql.queryJson(query, graphUri).then((sparqlResults) => {

        return Promise.resolve(
            sparqlResults.map(function (result) {
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
                name: result.title ? result.title : result.displayId
            }
        }))

    })

}

function getCollectionMembers(uri, graphUri, limit, offset, sortColumn, search) {

    var graphs = ''
    if (graphUri) {
        graphs = 'FROM <' + config.get('triplestore').defaultGraph + '> FROM <' + graphUri + '>'
    }
    console.log(search)
    console.log(search !== '')

    var templateParams = {
        graphs: graphs,
        collection: uri,
        offset: offset !== undefined ? ' OFFSET ' + offset : '',
        limit: limit !== undefined ? ' LIMIT ' + limit : '',
        sort: sortColumn !== undefined ? ' ORDER BY ' + sortColumn.dir.toUpperCase() + '(UCASE(str(?' + sortColumn.column + '))) ' : '',
        search: search !== '' && search !== undefined ? escape(
				'FILTER(CONTAINS(lcase(?displayId), lcase(%L))||CONTAINS(lcase(?name), lcase(%L))||CONTAINS(lcase(?description), lcase(%L)))',
				search, search, search
			) : ''
    }

    var query = loadTemplate('sparql/getCollectionMembers.sparql', templateParams)

    console.log(query)

    return sparql.queryJson(query, graphUri).then((result) => {

        if (result) {

            return Promise.resolve(result)

        } else {

            return Promise.reject('collection not found')

        }

    })

}

function getSubCollections(uri, graphUri) {

    var query = loadTemplate('./sparql/SubCollectionMetadata.sparql', {

        parentCollection: sparql.escapeIRI(uri)

    })

    return sparql.queryJson(query, graphUri).then((sparqlResults) => {

        var results = sparqlResults.map(function (result) {
            return {
                uri: result['Collection'],
                name: result['name'] || '',
                description: result['description'] || '',
                displayId: result['displayId'] || '',
                version: result['version'] || ''
            };
        });

        return Promise.resolve(results)

    })

}

function getCollectionMetaData(uri, graphUri) {

    var templateParams = {
        collection: uri
    }

    var query = loadTemplate('sparql/getCollectionMetaData.sparql', templateParams)

    return sparql.queryJson(query, graphUri).then((result) => {

        if (result && result[0]) {

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
    getCollectionMembers: getCollectionMembers,
    getSubCollections: getSubCollections
}

