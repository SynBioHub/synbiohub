
var extend = require('xtend')
var async = require('async')

var loadTemplate = require('./loadTemplate')

var escape = require('pg-escape')

var prefixify = require('./prefixify')

var config = require('./config')

function search(store, prefixes, criteria, offset, limit, callback) {

    var templateParams = {
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

			if (result.name && result.name != '' && result.name != result.displayId) {
			    result.name = result.displayId + ' (' + result.name + ')'
			} else {
			    result.name = result.displayId
			}

                        var prefixedUri = prefixify(result.uri, prefixes)
			result.url = '/'+result.uri.toString().replace(config.get('databasePrefix'),'')

                        result.triplestore = 'public'
                        result.prefix = prefixedUri.prefix

                        return result

                    }))

                }

            })
        }
    ], function done(err) {

            res.status(500).send(err.stack)
                
    })
}

search.lucene = function lucene(value) {

    var criteriaStr = ''
    var values = value.split('&');
    if (value.indexOf('created') > -1) {
	criteriaStr += '   ?subject dcterms:created ?cdate . '
    }
    if (value.indexOf('modified') > -1) {
	criteriaStr += '   ?subject dcterms:modified ?mdate . '
    }
    for (var i = 0; i < values.length-1; i++) {
	var query = values[i].split('=')
	if (query[0].indexOf(':') > -1) {
	    criteriaStr += '   ?subject ' + query[0] + ' ' + query[1] + ' . '
	} else if (query[0]==='objectType') {
	    criteriaStr += '   ?subject a sbol2:' + query[1] + ' . '
	} else if (query[0]==='collection') {
	    criteriaStr += '   ?collection a sbol2:Collection .' +
		'   ' + query[1] + ' sbol2:member ?subject .'
	} else if (query[0]==='createdBefore') {
	    criteriaStr += '   FILTER (xsd:dateTime(?cdate) <= "'+query[1]+'T23:59:59Z"^^xsd:dateTime) '
	} else if (query[0]==='createdAfter') {
	    criteriaStr += '   FILTER (xsd:dateTime(?cdate) >= "'+query[1]+'T00:00:00Z"^^xsd:dateTime) '
	} else if (query[0]==='modifiedBefore') {
	    criteriaStr += '   FILTER (xsd:dateTime(?mdate) <= "'+query[1]+'T23:59:59Z"^^xsd:dateTime) '
	} else if (query[0]==='modifiedAfter') {
	    criteriaStr += '   FILTER (xsd:dateTime(?mdate) >= "'+query[1]+'T00:00:00Z"^^xsd:dateTime) '
	} else {
	    criteriaStr += '   ?subject sbol2:' + query[0] + ' ' + query[1] + ' . '
	}
    }
    if (values[values.length-1]!='') {
	var searchTerms = values[values.length-1].split(/[ ]+/)
	criteriaStr += 'FILTER ('
	var andMode = true
	var notMode = false
	for (var i = 0; i < searchTerms.length; i++) {
	    if (searchTerms[i]==='and') {
		andMode = true
		continue
	    } else if (searchTerms[i]==='or') {
		andMode = false
		continue
	    } else if (searchTerms[i]==='not') {
		notMode = true
		continue
	    }
	    if (i > 0) {
		if (andMode) {
		    criteriaStr += '&&'
		    andMode = false
		} else {
		    criteriaStr += '||'
		}
	    } 
	    if (notMode) {
		criteriaStr += ' !'
	    }
	    criteriaStr += escape(
		'(CONTAINS(lcase(?displayId), lcase(%L))||CONTAINS(lcase(?name), lcase(%L))||CONTAINS(lcase(?description), lcase(%L)))', 
		searchTerms[i],searchTerms[i],searchTerms[i]
	    )
	}
	criteriaStr += ')';
    }

    return criteriaStr

}

module.exports = search
