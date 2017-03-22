
const SBOLDocument = require('sboljs')

const config = require('../../config')

const n3ToSBOL = require('../../conversion/n3-to-sbol')

const { fetchSBOLObjectRecursive } = require('./fetch-sbol-object-recursive')

const fs = require('mz/fs')

const sparql = require('../../sparql/sparql')

const tmp = require('tmp-promise')

const serializeSBOL = require('../../serializeSBOL')


/* Fetches SBOL source for an SBOL object URI, returning the filename of a
 * temporary file containing the SBOL XML.
 * 
 * Intuitively, the SBOL object fetcher would use this and then load the
 * returned source into sboljs to return an object.  However, the requirements
 * are quite different: the SBOL object fetcher doesn't care about well-formatted
 * SBOL, and creating well-formatted SBOL from the results of SPARQL queries is
 * difficult and expensive.
 *
 * Because of this, when we retrieve SBOL to render, for example, the page for
 * a ComponentDefinition, we use the object fetcher.  The object fetcher works
 * by loading the results of multiple SPARQL queries into an sboljs SBOLDocument,
 * and then when the SBOLDocument is complete it can be used to render the page.
 *
 * While the source fetcher is generally more expensive because of the overhead
 * of conversion to well-formed SBOL, the object fetcher simply cannot handle 
 * large documents because it relies on sboljs building an RDF graph in memory.
 * So when we want to produce an SBOL document to return to the user, we use
 * the source fetcher, which has an elaborate pipeline of retrieving chunks
 * of N3 triples, converting them to RDF+XML using a script, then converting
 * the RDF+XML to SBOL XML using libSBOLj.  The source fetcher also returns
 * a filename rather than a string to avoid loading huge documents into 
 * memory.
 *
 * TODO: should the final resulting file be streamed into gzip so the gzipped
 * file can be sent as the response?
 *
 */
function fetchSBOLSource(type, objectUri, graphUri) {

    const sbol = new SBOLDocument()

    sbol._resolving = {};
    sbol._rootUri = objectUri

    /* First check if this object is a root collection.  If so, we can use the
     * rootCollection predicate to retrieve it without a recursive crawl.
     */
    return sparql.queryJson([
        'SELECT ?rootCollection WHERE {',
        '?rootCollection a <http://sbols.org/v2#Collection> .',
        '?rootCollection <http://wiki.synbiohub.org/wiki/Terms/synbiohub#rootCollection> ?rootCollection .',
        'FILTER(?rootCollection = <' + sbol._rootUri + '>)',
        '}'
    ].join('\n'), graphUri).then((results) => {

        console.log('r')
        console.log(results)

        if(results.length > 0) {

            console.log('root collection; using fetchRootCollectionSBOLSource')

            /* It's a root collection.  Hooray, we can use the more efficient
             * root collection fetcher!
             */
            return fetchRootCollectionSBOLSource(sbol, type, graphUri, objectUri)

        } else {

            console.log('using fallback')

            /* It's not a root collection, so this is going to be a recursive
             * crawl.  We need sboljs to work out which URIs are unresolved, so
             * just fall back on using fetchSBOLObject and serializing it
             * afterwards.
             *
             * Unfortunately, we also need to save the serialized XML to a file
             * because the other fetcher returns a filename...
             *
             * TODO: we probably don't need to use sboljs to find out which
             * URIs aren't resolved.  Bypassing this would avoid building an
             * RDF graph in memory.
             *
             * TODO: this causes the query to check for a root collection to
             * run again
             */
            return fetchSBOLObjectRecursive(sbol, type, objectUri, graphUri).then((res) => {

                return tmp.file().then((tempFile) => {

                    return fs.writeFile(tempFile.path, serializeSBOL(res.sbol))
                             .then(() => Promise.resolve(tempFile.path))

                })

            })

        }

    })

}

function fetchRootCollectionSBOLSource(sbol, type, graphUri, objectUri) {

    return sparql.queryJson([
        'SELECT (COUNT(*) as ?count) WHERE {',
        '?s <http://wiki.synbiohub.org/wiki/Terms/synbiohub#rootCollection> <' + objectUri + '> .',
        '?s ?p ?o .',
        '}'
    ].join('\n'), graphUri).then((results) => {

        var countLeft = results[0].count
        var offset = 0
        var limit = config.get('staggeredQueryLimit')

        var n3 = []

        return doNextQuery()

        function doNextQuery() {

            console.log(countLeft + ' left of ' + results[0].count)

            if(countLeft > 0) {

                return sparql.query([
                    'CONSTRUCT { ?s ?p ?o } WHERE {',
                    '?s <http://wiki.synbiohub.org/wiki/Terms/synbiohub#rootCollection> <' + sbol._rootUri + '> .',
                    '?s ?p ?o .',
                    '} OFFSET ' + offset + ' LIMIT ' + limit
                ].join('\n'), graphUri, 'text/plain').then((res) => {

                    n3.push(res.body)

                    countLeft -= limit
                    offset += limit

                    return doNextQuery()

                })

            } else {

                return n3ToSBOL(n3)

            }
        }
    })


}

module.exports = {
    fetchSBOLSource: fetchSBOLSource
}



