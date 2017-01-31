
var libxmljs = require('libxmljs')

var async = require('async')

var RdfXmlParser = require('rdf-parser-rdfxml')

function collateSPARQLResults(results, res) {

    var mimeType = results[0].mimeType

    for(var i = 1; i < results.length; ++ i) {

        if(results[i].mimeType !== mimeType) {

            res.status(500).send('mixed content types from federated SPARQL query')
            return

        }
    }

    mimeType = mimeType.split(';')[0]

    if(mimeType === 'application/json') {

        var mergedResults = []

        results.forEach((result) => {

            mergedResults = mergedResults.concat(JSON.parse(result.body))

        })

        res.header('content-type', 'application/json')
        res.send(JSON.stringify(mergedResults, null, 2))

        return
    }

    if(mimeType === 'application/sparql-results+json') {

        var mergedResults = mergeResults(
            results.map((result) => JSON.parse(result.body)))

        res.header('content-type', 'application/sparql-results+json')
        res.send(JSON.stringify(mergedResults, null, 2))

        return
    }

    if(mimeType === 'application/sparql-results+xml') {

        var mergedResults = mergeResults(
            results.map((result) => parseXmlResult(result.body)))

        var xmlDoc = new libxmljs.Document()

        var rootNode = xmlDoc.node('sparql').attr({ xmlns: 'http://www.w3.org/2005/sparql-results#' })
        var headNode = rootNode.node('head')

        mergedResults.head.vars.forEach((varName) => {
            headNode.node('variable').attr({ name: varName })
        })

        var resultsNode = rootNode.node('results')

        mergedResults.results.bindings.forEach((binding) => {

            var resultNode = resultsNode.node('result')

            Object.keys(binding).forEach((varName) => {

                var bindingValue = binding[varName]
                var bindingNode = resultNode.node('binding').attr({ name: varName })

                if(bindingValue.type === 'uri') {

                    bindingNode.node('uri', bindingValue.value)

                } else if(bindingValue.type === 'literal') {

                    var literalNode = bindingNode.node('literal', bindingValue.value)

                    var attr = {}

                    if(bindingValue['xml:lang'] !== undefined)
                        attr['xml:lang'] = bindingValue['xml:lang']

                    if(bindingValue['datatype'] !== undefined)
                        attr['datatype'] = bindingValue['datatype']

                    literalNode.attr(attr)

                } else if(bindingValue.type === 'bnode') {

                    bindingNode.node('bnode', bindingValue.value)

                }

            })
        })

        res.header('content-type', 'application/sparql-results+xml')
        res.status(200).send(xmlDoc.toString())

        return
    }

    async.map(results, (result, next) => {

        var parser = new RdfXmlParser();

        parser.parse(result.body, function(err, graph) {

            if(err)
                return next(err)

            next(null, graph)
        })

    }, (err, graphs) => {

        var mergedGraph = graphs[0]

        for(var i = 0; i < graphs.length; ++ i) {

            mergedGraph.addAll(graphs[i])

        }

        var serializer = new XMLSerializer()

        serializer.serialize(mergedGraph, (err, xml) => {

            if(err)
                return res.status(500).send(err.stack)

            res.header('content-type', 'application/rdf+xml')
            res.status(200).send(xml)
        })
    })

}

function mergeResults(results) {

    var mergedResults = {
        head: {
            vars: []
        },
        results: {
            bindings: []
        }
    }

    results.forEach((result) => {

        result.head.vars.forEach((varName) => {

            if(mergedResults.head.vars.indexOf(varName) === -1)
                mergedResults.head.vars.push(varName)

        });

        [].push.apply(mergedResults.results.bindings, result.results.bindings)
    })

    return mergedResults
}


function parseXmlResult(result) {

    var sparqlNamespace = 'http://www.w3.org/2005/sparql-results#'
         
    var parsedResult = {
        head: {
            vars: []
        },
        results: {
            bindings: []
        }
    }

    var resultDoc = libxmljs.parseXmlString(result)

    var sparqlNode = resultDoc.get('//xmlns:sparql', sparqlNamespace)

    var headNode = sparqlNode.get('xmlns:head', sparqlNamespace)

    headNode.find('xmlns:variable', sparqlNamespace).forEach((variableNode) => {

        var varName = variableNode.attr('name').value()

        if(parsedResult.head.vars.indexOf(varName) === -1)
            parsedResult.head.vars.push(varName)
    })

    var resultsNode = sparqlNode.get('xmlns:results', sparqlNamespace)

    resultsNode.find('xmlns:result', sparqlNamespace).forEach((resultNode) => {

        var resultBinding = {}

        resultNode.find('xmlns:binding', sparqlNamespace).forEach((bindingNode) => {

            var varName = bindingNode.attr('name').value()

            var value

            var uriNode = bindingNode.get('xmlns:uri', sparqlNamespace)

            if(uriNode) {

                value = {
                    type: 'uri',
                    value: uriNode.text()
                }

            } else {

                var literalNode = bindingNode.get('xmlns:literal', sparqlNamespace)

                if(literalNode) {

                    value = {
                        type: 'literal',
                        value: literalNode.text()
                    }

                    var lang = literalNode.attr('xml:lang')

                    if(lang)
                        value['xml:lang'] = lang.value()

                    var datatype = literalNode.attr('datatype')

                    if(datatype)
                        value.datatype = datatype.value()

                } else {

                    var bnode = bindingNode.get('xmlns:bnode', sparqlNamespace)

                    if(bnode) {

                        value = {
                            type: 'bnode',
                            value: bindingNode.text()
                        }


                    } else {

                        res.status(500).send('unknown binding')
                        return

                    }
                }

            }

            resultBinding[varName] = value

        })

        parsedResult.results.bindings.push(resultBinding)
    })

    return parsedResult
}


module.exports = collateSPARQLResults



