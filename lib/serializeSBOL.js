
function serializeSBOL(sbol) {

    return sbol.serializeXML({
        'xmlns:synbiohub': 'http://synbiohub.org#',
        'xmlns:sybio': 'http://www.sybio.ncl.ac.uk#',
        'xmlns:rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
        'xmlns:ncbi': 'http://www.ncbi.nlm.nih.gov#',
        'xmlns:igem': 'http://synbiohub.org/terms/igem/',
        'xmlns:gb': 'http://www.ncbi.nlm.nih.gov'
    })

}

module.exports = serializeSBOL

