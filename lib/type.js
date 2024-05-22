const namespace = require('./summarize/namespace')
const sequenceOntology = require('./ontologies/sequence-ontology')

function lookupType (uri) {
  uri = '' + uri

  if (uri.startsWith(namespace.biopax)) {
    let biopaxTerm = uri.slice(namespace.biopax.length)

    return {
      uri: uri,
      term: uri,
      description: { name: biopaxTerm }
    }
  }

  for (let prefix of namespace.so) {
    if (uri.startsWith(prefix)) {
      var soTerm = uri.slice(prefix.length).split('_').join(':')

      return {
        uri: uri,
        term: soTerm,
        description: sequenceOntology[soTerm] ? sequenceOntology[soTerm] : { name: soTerm }
      }
    }
  }

  return {
    uri: uri,
    term: uri,
    description: uri
  }
}

module.exports = lookupType
