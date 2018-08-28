
var SBOLDocument = require('sboljs')

var sparql = require('../../sparql/sparql')

var config = require('../../config')

var resolveBatch = config.get('resolveBatch')

var webOfRegistries = config.get('webOfRegistries')

var databasePrefix = config.get('databasePrefix')

var request = require('request')

var async = require('async')

var assert =  require('assert')

const fs = require('mz/fs')

const saveN3ToRdfXml = require('../../conversion/save-n3-to-rdfxml')

/* Retrieves an SBOL object *recursively*, in that anything it references, and
 * anything those objects reference (and so on) are resolved.  This is hugely
 * expensive (> 10 minutes) for large Collections, which is why the Collection
 * page currently uses metadata instead of an sboljs object.
 *
 * TODO: the collection page can use SBOL, it just needs to use the not yet
 * implemented "fetch SBOL object and children".  There's also no reason that
 * for example a ComponentDefinition couldn't have millions of components
 * (human genome?), so we should probably make sure that none of the pages for
 * top levels rely on recursively resolved SBOL documents and make them use
 * the "object and children" fetcher instead.
 *
 * TODO: make the recursive crawl fail for things that are obviously too big
 * to resolve everything.
 */
function fetchSBOLObjectRecursive(sbol, type, uri, graphUri) {

    sbol._resolving = {};
    sbol._rootUri = uri

    sbol.lookupURI(sbol._rootUri)

    return sparql.queryJson([
        'SELECT ?coll ?type WHERE {',
        '?coll a ?type .',
        'FILTER(?coll = <' + sbol._rootUri + '>)',
        '}'
    ].join('\n'), graphUri).then((results) => {

        if(results.length > 0) {

	    // TODO: temporarily removed, need to add recursive crawl after this
	    // to ensure non-local objects are fetched.
	    if(results[0].type === 'http://sbols.org/v2#Collection') {
		return getCollectionSBOL(sbol, type, graphUri)

            } else {

		return getSBOLRecursive(sbol, type, graphUri)

            }

	} else {

	    return Promise.reject(new Error(sbol._rootUri + ' not found'))

	}

    })

}

function getCollectionSBOL(sbol, type, graphUri) {

    var graphs = ''
    //if (graphUri) {
//	graphs = 'FROM <' + config.get('triplestore').defaultGraph + '> FROM <' + graphUri + '>'
    //}

    const subquery = [
        '{',
            '?s ?p ?o .',
            'FILTER(?s = <' + sbol._rootUri + '>)',
        '}',
        'UNION',
        '{',
            '?coll <http://sbols.org/v2#member> ?topLevel .',
            '?s <http://wiki.synbiohub.org/wiki/Terms/synbiohub#topLevel> ?topLevel .',
            '?s ?p ?o .',
            'FILTER(?coll = <' + sbol._rootUri + '>)',
        '}' /*,
        'UNION',
        '{',
            '?coll <http://sbols.org/v2#member> ?topLevel .',
	    '?topLevel a <http://sbols.org/v2#ComponentDefinition> .',
            '?topLevel <http://sbols.org/v2#sequence> ?sequence .',
            '?s <http://wiki.synbiohub.org/wiki/Terms/synbiohub#topLevel> ?sequence .',
            '?s ?p ?o .',
            'FILTER(?coll = <' + sbol._rootUri + '>)',
        '}',
        'UNION',
        '{',
            '?coll <http://sbols.org/v2#member> ?topLevel .',
	    '?topLevel a <http://sbols.org/v2#ModuleDefinition> .',
            '?topLevel <http://sbols.org/v2#model> ?model .',
            '?s <http://wiki.synbiohub.org/wiki/Terms/synbiohub#topLevel> ?model .',
            '?s ?p ?o .',
            'FILTER(?coll = <' + sbol._rootUri + '>)',
        '}'*/
    ].join('\n')

    return sparql.queryJson([
        'SELECT (COUNT(*) as ?count) ' + graphs + ' WHERE {',
        subquery,
        '}'
    ].join('\n'), graphUri).then((results) => {

        var countLeft = results[0].count
        var offset = 0
        var limit = config.get('staggeredQueryLimit')

        var rdf = []

        return doNextQuery()

        function doNextQuery() {

            console.log(countLeft + ' left of ' + results[0].count)

            if(countLeft > 0) {

                return sparql.query([
                    'CONSTRUCT { ?s ?p ?o } ' + graphs + ' WHERE { { SELECT ?s ?p ?o WHERE {',
                    subquery,
                    '} ORDER BY ASC(?s) ASC(?p) ASC(?o)} } OFFSET ' + offset + ' LIMIT ' + limit
                ].join('\n'), graphUri, 'text/plain').then((res) => {

                    rdf.push(res.body)

                    countLeft -= limit
                    offset += limit

                    return doNextQuery()

                })

            } else {

                return saveN3ToRdfXml(rdf).then((tempFilename) => {

                    return fs.readFile(tempFilename).then((contents) => {

                        fs.unlink(tempFilename)

                        return new Promise((resolve, reject) => {
                            sbol.loadRDF(contents.toString(), (err) => {

                                if(err) {
                                    reject(err)
                                    return
                                }

                                const object = sbol.lookupURI(sbol._rootUri)

                                sbol.graphUri = graphUri
                                object.graphUri = graphUri

                                resolve({
                                    graphUri: graphUri,
                                    sbol: sbol,
                                    object: object
                                })
                            })

                        })

                    })
                })

            }

        }
    })

}

function getSBOLRecursive(sbol, type, graphUri) {

    var complete = false;
    var resolved = false;

    return new Promise((resolve, reject) => {

        async.series([

            function getLocalParts(next) {

                completePartialDocument(graphUri, sbol, type, new Set([]), (err) => {

                    if(err) {
                        next(err)

                    } else {

                        if(!complete) {

                            complete = true

                            next()
                        }

                    }
                })
            },

            function fetchNonLocalSBOL(next) {
                async.each(sbol.unresolvedURIs, (uri, nextUri) => {
		    // TODO: temporary until URIs fixed
		    if (uri.toString().startsWith('http://wiki.synbiohub.org/')) {
			nextUri()
		    } else {
			prefix = uri.toString()
			if (prefix.indexOf('/public/') !== -1) {
			    prefix = prefix.substring(0,prefix.indexOf('/public/'))
			} else if (prefix.indexOf('/user/') !== -1) {
			    prefix = prefix.substring(0,prefix.indexOf('/user/'))
			    if (uri.toString().replace(prefix+'/user/','').indexOf('/') === -1) {
				prefix = uri.toString()
			    }
			}
			if (webOfRegistries[prefix]) {
			    uri = uri.replace(prefix,webOfRegistries[prefix]) + '/sbol'
			    console.log('Fetching non-local:'+uri)
			    request({
				method: 'GET',
				uri: uri,
				'content-type': 'application/rdf+xml',
			    }, function(err, response, body) {
				if(err || response.statusCode >= 300) {
				    nextUri()
				} else {
				    if (!body.toString().startsWith('<!DOCTYPE html><')) {
					sbol.loadRDF(body, nextUri)
				    } else {
					nextUri()
				    }
				}
			    })
			} else {
			    nextUri()
			}
		    }
                }, next)
            }

        ], function done(err) {

            if (err) {

                reject(err)

            } else {

                resolve({
                    sbol: sbol,
                    object: sbol.lookupURI(sbol._rootUri)
                })
            }
        })

    })

}

function completePartialDocument(graphUri, sbol, type, skip, next) {

    //console.log(sbol.unresolvedURIs.length + ' unresolved URI(s)')

    if(sbol.unresolvedURIs.length === 0) {

        next();

    } else {

        var toResolve = sbol.unresolvedURIs.filter((uri) => !sbol._resolving[uri] && !uri.toString().startsWith('http://wiki.synbiohub.org/') &&
						   !skip.has(uri.toString()) && uri.toString().startsWith(databasePrefix))
                                .map((uri) => uri.toString())

	console.log(toResolve.length + ' URI(s) left to resolve')
	
        toResolve = toResolve.slice(0, resolveBatch)

        retrieveSBOL(graphUri, sbol, type, toResolve, (err) => {

            if(err) {
                next(err)
                return
            }

            var done = true

            // somehow we killed the optimiser by doing uri toString inside
            // the loop, so let's do it first...
            //
            // ~50 seconds -> instant, thanks v8
            //
            var uriStrings = sbol.unresolvedURIs.map((uri) => uri.toString())

            for(var i = 0; i < uriStrings.length; ++ i)
            {
                var uri = uriStrings[i]

                var uriString = uri

                if (toResolve.indexOf(uriString) === -1 && uriString.startsWith(databasePrefix) && !uri.toString().startsWith('http://wiki.synbiohub.org/')
		    && !skip.has(uriString)) {
                    done = false
                } else {
                    skip.add(uriString)
                }
            }

            if (done) {
                next()
                return
            }

            completePartialDocument(graphUri, sbol, type, skip, next)

        })
    }
}

function retrieveSBOL(graphUri, sbol, type, uris, next) {

    Object.assign(sbol._resolving, uris)

    var countQuery = sparqlDescribeSubjects(sbol, type, uris, true)

    var query = sparqlDescribeSubjects(sbol, type, uris, false)

    var offset = 0
    var limit = config.get('staggeredQueryLimit')
    var countLeft

    sparql.queryJson(countQuery, graphUri).then((res) => {

        //console.log('count is ' + res[0].count)

        countLeft = res[0].count

        // if(countLeft === 0) {

        //     console.log(countQuery)

        //     next(new Error('incomplete document?'))
        //     return

        // }

        var rdf = []

        return doQuery()

        function doQuery() {

            return sparql.query(query + ' OFFSET ' + offset + ' LIMIT ' + limit, graphUri, 'application/rdf+xml').then((res) => {

                countLeft -= limit
                offset += limit

                rdf.push(res.body)

                if(countLeft > 0) {

                    return doQuery()

                } else {

                    //console.log('loading rdf')

                    sbol.loadRDF(rdf, next)

                }

            }).catch((err) => {

                next(err)

            })
        }

    })


}

function sparqlDescribeSubjects(sbol, type, uris, isCount) {

    /*
    var triples = uris.map((uri, n) =>
        sparql.escapeIRI(uri) + ' ?p' + n + ' ?o' + n + ' .'
    )

    return [
        'CONSTRUCT {'
    ].concat(triples).concat([
        '} WHERE {'
    ]).concat(triples).concat([
        '}'
    ]).join('\n')*/

   var query = [
       isCount ?
           'SELECT (count(?s) as ?count) WHERE {'
       :
           'CONSTRUCT { ?s ?p ?o } WHERE {'
   ]

   var isFirst = true

   uris.forEach((uri) => {

       if(isFirst)
           isFirst = false
       else
           query.push('UNION')

       query.push(
           '{',
           '?s ?p ?o .'
        )

       if(uri === sbol._rootUri) {
           if(type !== null) {
               if (type === "TopLevel") {
                   query.push('?s a ?t .')
                   // TODO: the generic top level will not work
                   query.push('FILTER(?t = <http://sbols.org/v2#ComponentDefinition>' + ' ||' +
                       ' ?t = <http://sbols.org/v2#ModuleDefinition>' + ' ||' +
                       ' ?t = <http://sbols.org/v2#Model>' + ' ||' +
                       ' ?t = <http://sbols.org/v2#Collection>' + ' ||' +
                       ' ?t = <http://sbols.org/v2#Sequence>' + ' ||' +
                       ' ?t = <http://sbols.org/v2#GenericTopLevel>)')
               } else if (type != 'GenericTopLevel') {
                   query.push('?s a <http://sbols.org/v2#' + type + '> .')
               }
           }
       }

        query.push(
           'FILTER(?s = ' + sparql.escapeIRI(uri) + ')',
           '}'
        )
   })

   query.push('}')

   return query.join('\n')
}

module.exports = {
    fetchSBOLObjectRecursive: fetchSBOLObjectRecursive
}



