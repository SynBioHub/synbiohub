
const igemNS = 'http://wiki.synbiohub.org/wiki/Terms/igem#'

const biopaxNS = 'http://www.biopax.org/release/biopax-level3.owl#'

const soNS = 'http://identifiers.org/so/'

const sbolNS = 'http://sbols.org/v2#'

const provNS = 'http://www.w3.org/ns/prov#'

const sbhNS = 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#'

const benchNS = 'http://wiki.synbiohub.org/wiki/Terms/benchling#'

const dctermsNS = 'http://purl.org/dc/terms/'

const dcNS = 'http://purl.org/dc/elements/1.1/'

const celloNS = 'http://cellocad.org/Terms/cello#'

const rdfNS = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'

const rdfsNS = 'http://www.w3.org/2000/01/rdf-schema#'

const purlNS = 'http://purl.obolibrary.org/obo/'

const genbankNS = 'http://www.ncbi.nlm.nih.gov/genbank#'

var sequenceOntology = require('../ontologies/sequence-ontology')

function shortName (uri) {
  var name
  if (uri.toString().startsWith(igemNS)) {
    name = uri.replace(igemNS, 'igem:')
  } else if (uri.toString().startsWith(soNS)) {
    var soTerm = uri.toString().replace(soNS, '')
    if (sequenceOntology[soTerm]) {
      name = 'so:' + sequenceOntology[soTerm].name
    } else {
      name = soTerm
    }
  } else if (uri.toString().startsWith(provNS)) {
    name = uri.replace(provNS, 'prov:')
  } else if (uri.toString().startsWith(sbolNS)) {
    name = uri.replace(sbolNS, 'sbol2:')
  } else if (uri.toString().startsWith(sbhNS)) {
    name = uri.replace(sbhNS, 'sbh:')
  } else if (uri.toString().startsWith(benchNS)) {
    name = uri.replace(benchNS, 'bench:')
  } else if (uri.toString().startsWith(celloNS)) {
    name = uri.replace(celloNS, 'cello:')
  } else if (uri.toString().startsWith(dctermsNS)) {
    name = uri.replace(dctermsNS, 'dcterms:')
  } else if (uri.toString().startsWith(dcNS)) {
    name = uri.replace(dcNS, 'dc:')
  } else if (uri.toString().startsWith(rdfNS)) {
    name = uri.replace(rdfNS, 'rdf:')
  } else if (uri.toString().startsWith(rdfsNS)) {
    name = uri.replace(rdfsNS, 'rdfs:')
  } else if (uri.toString().startsWith(purlNS)) {
    name = uri.replace(purlNS, 'purl:')
  } else if (uri.toString().startsWith(genbankNS)) {
    name = uri.replace(genbankNS, 'genbank:')
  } else if (uri.toString().startsWith(biopaxNS)) {
    name = uri.replace(biopaxNS, 'biopax:')
  } else {
    name = uri
  }
  return name
}

function longName (name) {
  var uri
  if (name.toString().startsWith('igem:')) {
    uri = name.replace('igem:', igemNS)
  } else if (name.toString().startsWith('so:')) {
    uri = name.replace('so:', soNS)
  } else if (name.toString().startsWith('prov:')) {
    uri = name.replace('prov:', provNS)
  } else if (name.toString().startsWith('sbol2:')) {
    uri = name.replace('sbol2:', sbolNS)
  } else if (name.toString().startsWith('sbh:')) {
    uri = name.replace('sbh:', sbhNS)
  } else if (name.toString().startsWith('bench:')) {
    uri = name.replace('bench:', benchNS)
  } else if (name.toString().startsWith('cello:')) {
    uri = name.replace('cello:', celloNS)
  } else if (name.toString().startsWith('dcterms:')) {
    uri = name.replace('dcterms:', dctermsNS)
  } else if (name.toString().startsWith('dc:')) {
    uri = name.replace('dc:', dcNS)
  } else if (name.toString().startsWith('rdf:')) {
    uri = name.replace('rdf:', rdfNS)
  } else if (name.toString().startsWith('rdfs:')) {
    uri = name.replace('rdfs:', rdfsNS)
  } else if (name.toString().startsWith('purl:')) {
    uri = name.replace('purl:', purlNS)
  } else if (name.toString().startsWith('genbank:')) {
    uri = name.replace('genbank:', genbankNS)
  } else if (name.toString().startsWith('biopax:')) {
    uri = name.replace('biopax:', biopaxNS)
  } else {
    uri = name
  }
  return uri
}

module.exports = {
  shortName: shortName,
  longName: longName,
  rdf: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
  dcterms: 'http://purl.org/dc/terms/',
  dc: 'http://purl.org/dc/elements/1.1/',
  prov: 'http://www.w3.org/ns/prov#',
  sbol: 'http://sbols.org/v2#',
  sybio: 'http://www.sybio.ncl.ac.uk#',
  rdfs: 'http://www.w3.org/2000/01/rdf-schema#',
  ncbi: 'http://www.ncbi.nlm.nih.gov#',
  go: [
    'http://purl.org/obo/owl/GO#',
    'http://identifiers.org/go/'
  ],
  so: [
    'http://purl.org/obo/owl/SO#',
    'http://identifiers.org/so/'
  ],
  biopax: 'http://www.biopax.org/release/biopax-level3.owl#',
  synbiohub: 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#',
  igem: 'http://wiki.synbiohub.org/wiki/Terms/igem#'
}
