
var summarizeTopLevel = require('./summarizeTopLevel')

var formatSequence = require('sequence-formatter')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI
var Sequence = require('sboljs/lib/Sequence')

function summarizeSequence (sequence, req, sbol, remote, graphUri) {
  if (sequence instanceof URI) {
    return uriToMeta(sequence)
  }
  if (!(sequence instanceof Sequence)) {
    return uriToMeta(sequence.uri)
  }

  var encodingUri = sequence.encoding + ''

  var encoding = mapEncoding(encodingUri) || encodingUri

  var elements = sequence.elements

  var summary = {
    type: encoding,
    length: sequence.elements.length,
    lengthUnits: lengthUnits(encoding),
    encoding: sequence.encoding.toString(),
    elements: elements,
    formatted: formatSequence(elements),
    blastUrl: encoding === 'AminoAcid'
      ? 'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastp&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome'
      : 'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastn&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome'
  }

  return Object.assign(summary, summarizeTopLevel(sequence, req, sbol, remote, graphUri))
}

function mapEncoding (encoding) {
  return ({
    'http://www.chem.qmul.ac.uk/iupac/AminoAcid/': 'AminoAcid',
    'http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html': 'NucleicAcid',
    'http://dx.doi.org/10.1021/bi00822a023': 'NucleicAcid'
  })[encoding]
}

function lengthUnits (encoding) {
  return ({
    'AminoAcid': 'aa',
    'NucleicAcid': 'bp'
  })[encoding]
}

module.exports = summarizeSequence
