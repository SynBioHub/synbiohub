const namespace = require('./summarize/namespace')
const sequenceOntology = require('./ontologies/sequence-ontology')
const geneOntology = require('./ontologies/gene-ontology')

function lookupRole (uri) {
  uri = '' + uri

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

  for (let prefix of namespace.go) {
    if (uri.startsWith(prefix)) {
      var goTerm = uri.slice(prefix.length).split('_').join(':')

      return {
        uri: uri,
        term: goTerm,
        description: geneOntology[goTerm] ? geneOntology[goTerm] : { name: goTerm }
      }
    }
  }

  var igemPrefix = 'http://wiki.synbiohub.org/wiki/Terms/igem#partType/'

  if (!uri.term && uri.indexOf(igemPrefix) === 0) {
    return {
      uri: uri,
      term: uri.slice(igemPrefix.length),
      description: { name: uri.slice(igemPrefix.length) }
    }
  }

  if (!uri.term && uri.lastIndexOf('#') >= 0 && uri.lastIndexOf('#') + 1 < uri.length) {
    return {
      uri: uri,
      term: uri.slice(uri.lastIndexOf('#') + 1),
      description: { name: uri.slice(uri.lastIndexOf('#') + 1) }
    }
  }

  if (!uri.term && uri.lastIndexOf('/') >= 0 && uri.lastIndexOf('/') + 1 < uri.length) {
    return {
      uri: uri,
      term: uri.slice(uri.lastIndexOf('/') + 1),
      description: { name: uri.slice(uri.lastIndexOf('/') + 1) }
    }
  }

  return {
    uri: uri,
    term: uri,
    description: { name: uri }
  }
}

module.exports = lookupRole
