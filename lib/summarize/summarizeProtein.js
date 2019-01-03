
var namespace = require('./namespace')

function summarizeProtein(componentDefinition) {

    var summary = {
    }

    var pI = componentDefinition.getAnnotations(namespace.sybio + 'pI')

    if(pI.length > 0) {
        summary.pI = pI[0]
    }

    var encodedBy = componentDefinition.getAnnotations(namespace.sybio + 'en_by')

    if(encodedBy.length > 0) {

        summary.encodedBy = encodedBy

    }

    var locatedIn = componentDefinition.getAnnotations(namespace.sybio + 'located_in')

    if(locatedIn.length > 0) {

        summary.locatedIn = locatedIn.map((uri) => {

            if(uri.indexOf(namespace.go) === 0) {

                var goTerm = uri.slice(namespace.go.length).split('_').join(':')

                var geneOntology = require('./gene-ontology')

                return {
                    uri: uri,
                    term: goTerm,
                    description: geneOntology[goTerm]
                }

            } else {

                return {
                    uri: uri
                }

            }

        })

    }

    return summary
}

module.exports = summarizeProtein

