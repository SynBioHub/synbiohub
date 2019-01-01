var summarizeActivity = require('./summarizeActivity')
var summarizeAttachment = require('./summarizeAttachment')
var summarizeCollection = require('./summarizeCollection')
var summarizeCombinatorialDerivation = require('./summarizeCombinatorialDerivation')
var summarizeComponent = require('./summarizeComponent')
var summarizeComponentDefinition = require('./summarizeComponentDefinition')
var summarizeFunctionalComponent = require('./summarizeFunctionalComponent')
var summarizeGenericTopLevel = require('./summarizeGenericTopLevel')
var summarizeImplementation = require('./summarizeImplementation')
var summarizeInteraction = require('./summarizeInteraction')
var summarizeLocation = require('./summarizeLocation')
var summarizeMapsTo = require('./summarizeMapsTo')
var summarizeModel = require('./summarizeModel')
var summarizeModule = require('./summarizeModule')
var summarizeModuleDefinition = require('./summarizeModuleDefinition')
var summarizeParticipation = require('./summarizeParticipation')
var summarizeSequence = require('./summarizeSequence')
var summarizeSequenceAnnotation = require('./summarizeSequenceAnnotation')
var summarizeSequenceConstraint = require('./summarizeSequenceConstraint')
var summarizeVariableComponent = require('./summarizeVariableComponent')

function summarizeSBOL(typeShortName,topLevel,req,sbol,remote,graphUri) {
    if (typeShortName === 'Activity') {
        meta = summarizeActivity(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Attachment') {
        meta = summarizeAttachment(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Collection') {
        meta = summarizeCollection(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'CombinatorialDerivation') {
        meta = summarizeCombinatorialDerivation(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Component') {
        meta = summarizeComponentDefinition(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'ComponentInstance') {
        meta = summarizeComponent(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'FunctionalComponent') {
        meta = summarizeFunctionalComponent(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Implementation') {
        meta = summarizeImplementation(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Interaction') {
        meta = summarizeInteraction(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Location' || typeShortName === 'Range' || typeShortName === 'Cut' ||
	       typeShortName === 'GenericLocation') {
        meta = summarizeLocation(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'MapsTo') {
        meta = summarizeMapsTo(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Model') {
        meta = summarizeModel(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Module') {
        meta = summarizeModuleDefinition(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'ModuleInstance') {
        meta = summarizeModule(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Participation') {
        meta = summarizeParticipation(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'Sequence') {
        meta = summarizeSequence(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'SequenceAnnotation') {
        meta = summarizeSequenceAnnotation(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'SequenceConstraint') {
        meta = summarizeSequenceConstraint(topLevel,req,sbol,remote,graphUri)
    } else if (typeShortName === 'VariableComponent') {
        meta = summarizeVariableComponent(topLevel,req,sbol,remote,graphUri)
    } else {
        meta = summarizeGenericTopLevel(topLevel,req,sbol,remote,graphUri)
    }
    return meta
}
module.exports = summarizeSBOL

