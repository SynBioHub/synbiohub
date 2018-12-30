var summarizeCombinatorialDerivation = require('./summarizeCombinatorialDerivation')
var summarizeComponentDefinition = require('./summarizeComponentDefinition')
var summarizeSequence = require('./summarizeSequence')
var summarizeActivity = require('./summarizeActivity')
var summarizeAttachment = require('./summarizeAttachment')
var summarizeModuleDefinition = require('./summarizeModuleDefinition')
var summarizeImplementation = require('./summarizeImplementation')
var summarizeModel = require('./summarizeModel')
var summarizeGenericTopLevel = require('./summarizeGenericTopLevel')
var sequenceOntology = require('./sequence-ontology')
var geneOntology = require('./gene-ontology')
var systemsBiologyOntology = require('./systems-biology-ontology')

function summarizeSBOL(typeShortName,topLevel,req,sbol,remote,graphUri) {
    if (typeShortName === 'Activity') {
        meta = summarizeActivity(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Attachment') {
        meta = summarizeAttachment(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'CombinatorialDerivation') {
        meta = summarizeCombinatorialDerivation(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Component') {
        meta = summarizeComponentDefinition(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Implementation') {
        meta = summarizeImplementation(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Model') {
        meta = summarizeModel(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Module') {
        meta = summarizeModuleDefinition(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Sequence') {
        meta = summarizeSequence(topLevel,req,sbol,remote,graphUri)
    } else {
        meta = summarizeGenericTopLevel(topLevel,req,sbol,remote,graphUri)
    }
    return meta
}
module.exports = summarizeSBOL

