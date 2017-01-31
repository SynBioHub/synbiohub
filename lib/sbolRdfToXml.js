
var SBOLDocument = require('sboljs');

var fork = require("child_process").fork;

var blockingThreshold = 1024 * 4;

const getRdfSerializeAttribs = require('./getRdfSerializeAttribs')

function sbolRdfToXml(rdf, callback)
{
    if(rdf.length > blockingThreshold)
    {
        sbolRdfToXmlAsync(rdf, callback);
    }
    else
    {
        SBOLDocument.loadRDF(rdf, function(err, sbol) {
            if(err) {
                
                callback(err);

            } else {
                callback(null, sbol.serializeXML(getRdfSerializeAttribs()))
            }

        });
    }
}

function sbolRdfToXmlAsync(rdf, callback)
{
    var childProcess = fork(__dirname + "/sbolRdfToXmlWorker");

    childProcess.send({ rdf: rdf });

    childProcess.on('message', function(data) {

        childProcess.send({ done: true });
    
        callback(null, data.xml);
    });

    childProcess.on('exit', function() {
    
    });
}

module.exports = sbolRdfToXml;



