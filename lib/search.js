
var extend = require('xtend')
var async = require('async')

var loadTemplate = require('./loadTemplate')

var escape = require('pg-escape')

var base64 = require('./base64')

var prefixify = require('./prefixify')

function search(store, prefixes, type, criteria, offset, limit, callback) {

    var templateParams = {
        type: type,
        criteria: criteria,
        offset: offset !== undefined ? ' OFFSET ' + offset : '',
        limit: limit !== undefined ? ' LIMIT ' + limit : ''
    }

    var countQuery = loadTemplate('sparql/searchCount.sparql', templateParams)
    var searchQuery = loadTemplate('sparql/search.sparql', templateParams)

    var count = 0

    async.series([

        function performCount(next) {

            store.sparql(countQuery, (err, result) => {

                if(err) {

                    callback(err)

                } else {

                    console.log(result)

                    if(result && result[0] && result[0].count !== undefined) {
                        count = result[0].count
                    }

                    next()
                }

            })
        },

        function performSearch(next) {

            store.sparql(searchQuery, (err, results) => {

                if(err) {

                    callback(err)

                } else {

                    callback(null, count, results.map((result) => {

                        result.uri = result.subject
                        delete result.subject

                        result.base64Uri = base64.encode(result.uri)

                        var prefixedUri = prefixify(result.uri, prefixes)

                        if(type === 'ComponentDefinition') {

                            if(prefixedUri.prefix)
                                result.url = '/component/' + prefixedUri.prefix + '/' + prefixedUri.uri
                            else
                                result.url = '/component/' + result.base64Uri

                        } else if(type === 'Collection') {

                            result.url = '/collection/' + result.base64Uri

                        }

                        result.triplestore = 'public'
                        result.prefix = prefixedUri.prefix

                        return result

                    }))

                }

            })
        }
    ])
}

search.lucene = function lucene(predicate, value) {

    return escape(
        '?subject ' + predicate + ' ?p .' +
	    'FILTER (CONTAINS(?p, %L))', 
            value
    )

}

module.exports = search

