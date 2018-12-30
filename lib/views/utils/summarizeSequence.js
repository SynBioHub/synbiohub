
var namespace = require('./namespace')
var summarizeTopLevel = require('./summarizeTopLevel')

var formatSequence = require('sequence-formatter')

var URI = require('sboljs').URI

function summarizeSequence(sequence,req,sbol,remote,graphUri) {

    var encodingUri = sequence.encoding + ''

    var encoding = mapEncoding(encodingUri) || encodingUri

    var elements = sequence.elements

    if (sequence instanceof URI)
	return {
            uri: sequence + '',
	    id: sequence + ''
	}


    var summary = {
        type: encoding,
        length: sequence.elements.length,
        lengthUnits: lengthUnits(encoding),
	encoding: sequence.encoding.toString(),
        elements: elements,
	formatted: formatSequence(elements),
	blastUrl: encoding === 'http://www.chem.qmul.ac.uk/iupac/AminoAcid/' ?
            'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastp&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome' :
            'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastn&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome'
    }

    return Object.assign(summary,summarizeTopLevel(sequence,req,sbol,remote,graphUri))
}

function mapEncoding(encoding) {

    return ({
        'http://www.chem.qmul.ac.uk/iupac/AminoAcid/': 'AminoAcid',
        'http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html': 'NucleicAcid',
        'http://dx.doi.org/10.1021/bi00822a023': 'NucleicAcid'
    })[encoding]

}

function lengthUnits(encoding) {

    return ({
        'AminoAcid': 'aa',
        'NucleicAcid': 'bp'
    })[encoding]

}

module.exports = summarizeSequence

