
function serializeSBOL(sbol) {

    return sbol.serializeXML({
        'xmlns:synbiohub': 'http://synbiohub.org#',
        'xmlns:sybio': 'http://www.sybio.ncl.ac.uk#',
        'xmlns:rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
        'xmlns:ncbi': 'http://www.ncbi.nlm.nih.gov#',
        'xmlns:igem': 'http://synbiohub.org/terms/igem/',
        'xmlns:genbank': 'http://www.ncbi.nlm.nih.gov/genbank#',
        "xmlns:dcterms": "http://purl.org/dc/terms/",
        "xmlns:dc": "http://purl.org/dc/elements/1.1/"
    })

}

module.exports = serializeSBOL

