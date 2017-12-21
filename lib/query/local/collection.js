
const loadTemplate = require('../../loadTemplate')
const sparql = require('../../sparql/sparql')
const config = require('../../config')
var escape = require('pg-escape')

function getCollectionMemberCount(uri, graphUri, search) {

    const isSearch = (search !== '')

    var templateParams =  {
        collection: uri,
        search: search !== '' && search !== undefined ? escape(
				'FILTER(CONTAINS(lcase(?displayId), lcase(%L))||CONTAINS(lcase(?name), lcase(%L))||CONTAINS(lcase(?description), lcase(%L)))',
				search, search, search
			) : ''
    }

    var query = isSearch?loadTemplate('./sparql/CountMembersSearch.sparql', templateParams):loadTemplate('./sparql/CountMembers.sparql', templateParams)
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

    function sortByNames(a, b) {
        if (a.name < b.name) {
            return -1
        } else {
            return 1
        }
    }

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

    }).then((results) => {
	return results.sort(sortByNames)
    })
}

function getCollectionMembers(uri, graphUri, limit, offset, sortColumn, search) {

    var graphs = ''
    var sort = ''
    if (graphUri) {
        graphs = 'FROM <' + config.get('triplestore').defaultGraph + '> FROM <' + graphUri + '>'
    }

    const isSearch = (search !== '')

    sort = ' ORDER BY ASC(lcase(str(?type))) ASC(str(lcase(?name))) '

    //sort = ' ORDER BY ASC(lcase(str(?type))) ASC(concat(str(lcase(?name)),str(lcase(?displayId)))) '

    if (sortColumn !== undefined && 
	sortColumn.dir !== undefined &&
	sortColumn.column !== undefined) {
	if (sortColumn.column == 'name') {
	    sort = ' ORDER BY ' + sortColumn.dir.toUpperCase() + 
		'(str(lcase(?name))) '
//		'(concat(str(lcase(?name)),str(lcase(?displayId)))) '
	} else if (sortColumn.column == 'type') {
	    sort = ' ORDER BY ' + sortColumn.dir.toUpperCase() + '(lcase(str(?type))) ' +
		'ASC(str(lcase(?name))) '
//		'ASC(concat(str(lcase(?name)),str(lcase(?displayId)))) '
	} else {
	    sort = ' ORDER BY ' + sortColumn.dir.toUpperCase() + '(str(lcase(?' + sortColumn.column + '))) '
	}
    }

    var templateParams = {
        graphs: graphs,
        collection: uri,
        offset: offset !== undefined ? ' OFFSET ' + offset : '',
        limit: limit !== undefined ? ' LIMIT ' + limit : '',
	//sort: sortColumn !== undefined && sortColumn.dir !== undefined && sortColumn.column !== undefined ? ' ORDER BY ' + sortColumn.dir.toUpperCase() + '(UCASE(str(?' + sortColumn.column + '))) ' : '',
        sort: sort,
        search: search !== '' && search !== undefined ? escape(
				'FILTER(CONTAINS(lcase(?displayId), lcase(%L))||CONTAINS(lcase(?name), lcase(%L))||CONTAINS(lcase(?description), lcase(%L)))',
				search, search, search
			) : ''
    }

    var query = isSearch?loadTemplate('sparql/getCollectionMembersSearch.sparql', templateParams):
	loadTemplate('sparql/getCollectionMembers.sparql', templateParams)

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

