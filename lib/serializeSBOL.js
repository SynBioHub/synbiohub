
function serializeSBOL(sbol) {

    return sbol.serializeXML({
        'xmlns:synbiohub': 'http://synbiohub.org#'
    })

}

module.exports = serializeSBOL

