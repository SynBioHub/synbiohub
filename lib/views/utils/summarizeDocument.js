
var summarizeComponentDefinition = require('./summarizeComponentDefinition')

function summarizeDocument(sbol) {

    return {

        componentDefinitions: sbol.componentDefinitions.map(summarizeComponentDefinition)

    }

}

module.exports = summarizeDocument


