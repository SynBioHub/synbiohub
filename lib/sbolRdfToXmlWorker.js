
var SBOLDocument = require('sboljs');

const getRdfSerializeAttribs = require('./getRdfSerializeAttribs')

process.on('message', function(data) {

    if(data.done) {
        process.exit(0);
        return;
    }

    var rdf = data.rdf;

    SBOLDocument.loadRDF(rdf, function(err, sbol) {

        var xml = sbol.serializeXML(getRdfSerializeAttribs())

        process.send({ xml: xml });
    });
});



