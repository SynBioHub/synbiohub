
var SBOLDocument = require('sboljs')

var sparql = require('../../sparql/sparql')

const fs = require('mz/fs')

const tmp = require('tmp-promise')

const serializeSBOL = require('../../serializeSBOL')

const { fetchSBOLObjectNonRecursive } = require('./fetch-sbol-object-non-recursive')

function fetchSBOLSourceNonRecursive (type, uri, graphUri, members) {
  const sbol = new SBOLDocument()

  sbol._resolving = {}
  sbol._rootUri = uri

  sbol.lookupURI(sbol._rootUri)

  return sparql.queryJson([
    'SELECT ?coll ?type WHERE {',
    '?coll a ?type .',
    'FILTER(?coll = <' + sbol._rootUri + '>)',
    '}'
  ].join('\n'), graphUri).then((results) => {
    if (results.length > 0) {
      return fetchSBOLObjectNonRecursive(sbol, type, uri, graphUri, members).then((res) => {
        return tmp.tmpName().then((tmpFilename) => {
          return fs.writeFile(tmpFilename, serializeSBOL(res.sbol))
            .then(() => Promise.resolve(tmpFilename))
        })
      })
    } else {
      return Promise.reject(new Error(sbol._rootUri + ' not found'))
    }
  })
}

module.exports = {
  fetchSBOLSourceNonRecursive: fetchSBOLSourceNonRecursive
}
