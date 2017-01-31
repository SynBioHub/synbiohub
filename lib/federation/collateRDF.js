
var async = require('async')

var RdfXmlParser = require('rdf-parser-rdfxml')
var XMLSerializer = require('rdf-serializer-xml')

function collateRDF(results, res) {

    var mimeType = results[0].mimeType

    for(var i = 1; i < results.length; ++ i) {

        if(results[i].mimeType !== mimeType) {

            res.status(500).send('mixed content types from federated RDF query')
            return

        }
    }

    async.map(results, (result, next) => {

        var parser = new RdfXmlParser();

        parser.parse(result.body, function(err, graph) {

            if(err)
                return next(err)

            next(null, graph)
        })

    }, (err, graphs) => {

        if(err)
            return res.status(500).send(err)

        var mergedGraph = graphs[0]

        for(var i = 0; i < graphs.length; ++ i) {

            mergedGraph.addAll(graphs[i])

        }

        var serializer = new XMLSerializer()

        serializer.serialize(mergedGraph, (err, xml) => {

            if(err)
                return res.status(500).send(err)

            res.header('content-type', 'application/rdf+xml')
            res.status(200).send(xml)
        })
    })

}



module.exports = collateRDF



