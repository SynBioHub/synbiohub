
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
function fetchSBOLSource (type, objectUri, graphUri) {
  const sbol = new SBOLDocument()

  sbol._resolving = {}
  sbol._rootUri = objectUri

  /* First check if this object is a collection.  If so, we can use the
* specialized collection query to retrieve it without a recursive crawl.
*/
  return sparql.queryJson([
    'SELECT ?coll WHERE {',
    '?coll a <http://sbols.org/v2#Collection> .',
    'FILTER(?coll = <' + sbol._rootUri + '>)',
    '}'
  ].join('\n'), graphUri).then((results) => {
    // TODO: temporarily removed, need to add recursive crawl after this
    // to ensure non-local objects are fetched.
    if (results.length > 0) {
      /* It's a collection.  Hooray, we can use the more efficient
* collection fetcher!
*/
      return fetchCollectionSBOLSource(sbol, type, graphUri, objectUri)
    } else {
      /* It's not a collection, so this is going to be a recursive
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
* TODO: this causes the query to check for a collection to
* run again
*/
      return fetchSBOLObjectRecursive(sbol, type, objectUri, graphUri).then((res) => {
        return tmp.tmpName().then((tmpFilename) => {
          return fs.writeFile(tmpFilename, serializeSBOL(res.sbol))
            .then(() => Promise.resolve(tmpFilename))
        })
      })
    }
  })
}

function fetchCollectionSBOLSource (sbol, type, graphUri, objectUri) {
  var graphs = ''
  // if (graphUri) {
  // graphs = 'FROM <' + config.get('triplestore').defaultGraph + '> FROM <' + graphUri + '>'
  // }

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
'}' */
  ].join('\n')

  return sparql.queryJson([
    'SELECT (COUNT(*) as ?count) ' + graphs + ' WHERE {',
    subquery,
    '}'
  ].join('\n'), graphUri).then((results) => {
    var countLeft = results[0].count
    var offset = 0
    var limit = config.get('staggeredQueryLimit')

    var n3 = []

    return doNextQuery()

    function doNextQuery () {
      // console.log(countLeft + ' left of ' + results[0].count)

      if (countLeft > 0) {
        query = ['CONSTRUCT { ?s ?p ?o } ' + graphs + ' WHERE { { SELECT ?s ?p ?o WHERE {',
          subquery,
          '} ORDER BY ASC(?s) ASC(?p) ASC(?o)} } OFFSET ' + offset + ' LIMIT ' + limit].join('\n')
        // console.log(query)

        return sparql.query(query, graphUri, 'text/plain').then((res) => {
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
