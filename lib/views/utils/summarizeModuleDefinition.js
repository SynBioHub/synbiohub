
var namespace = require('./namespace')
var summarizeIdentified = require('./summarizeIdentified')
var summarizeComponentDefinition = require('./summarizeComponentDefinition')
var summarizeGenericTopLevel = require('./summarizeGenericTopLevel')
var summarizeModel = require('./summarizeModel')

var getDisplayList = require('visbol/lib/getDisplayList').getDisplayList

var getInteractionList = require('visbol/lib/getInteractionList')

var URI = require('sboljs').URI

var config = require('../../config')

var uriToUrl = require('../../uriToUrl')

var systemsBiologyOntology = require('./systems-biology-ontology')

function summarizeModuleDefinition(moduleDefinition,req,sbol,remote,graphUri) {

    if (moduleDefinition instanceof URI)
	return {
            uri: moduleDefinition + '',
	    id: moduleDefinition + ''
	}

    var summary = {
        numSubModules: moduleDefinition.modules.length,
        numSubModulesTotal: 0,
	displayList: createDisplayList(moduleDefinition,req),
	modules: summarizeModules(moduleDefinition,req,sbol,remote,graphUri),
	roles: moduleDefinition.roles,
	models: summarizeModels(moduleDefinition,req,sbol,remote,graphUri),
	functionalComponents: summarizeFunctionalComponents(moduleDefinition,req,sbol,remote,graphUri),
	interactions: summarizeInteractions(moduleDefinition,req)
    }

    summary = Object.assign(summary,summarizeGenericTopLevel(moduleDefinition,req,sbol,remote,graphUri))

    var uploadedBy = moduleDefinition.getAnnotation(namespace.synbiohub + 'uploadedBy')

    if(uploadedBy) {

        summary.synbiohub = {
            uploadedBy: uploadedBy
        }
    }

    return summary
}

function createDisplayList(moduleDefinition,req) {
    var component = {
	segments : []
    }

    var interactions = [];
    var displayList

    if (config.get('showModuleInteractions') /*&& moduleDefinition.interactions.length > 0*/) {
	moduleDefinition.functionalComponents.forEach(function(functionalComponent) {
	    component.segments = component.segments.concat(
		getDisplayList(functionalComponent.definition, config,
			       req.url.toString().endsWith('/share')).components[0].segments[0])
	})
	var currentInteractions = getInteractionList(moduleDefinition,config,req.url.toString().endsWith('/share'))
	for (let i in currentInteractions) {

            interactions.push(currentInteractions[i]);

	}
	//	    moduleDefinition.modules.forEach(function(module) {
	//		interactions = interactions.concat(
	//		    getInteractionList(module.definition)
	//		)
	//	    })

	displayList = {
	    version: 1,
	    components: [
            	component
	    ],
	    interactions: interactions
	}
    }
    return displayList
}

function summarizeModules(moduleDefinition,req,sbol,remote,graphUri) {
    modules = []
    moduleDefinition.modules.forEach((module) => {
	var moduleResult = {}
	module.link()
	moduleResult = summarizeIdentified(module,req)
	moduleResult.definition = summarizeIdentified(module.definition,req)
	modules.push(moduleResult)
    })
    return modules
}

function summarizeModels(moduleDefinition,req,sbol,remote,graphUri) {
    models = []
    moduleDefinition.models.forEach((model) => {
	models.push(summarizeModel(model,req,sbol,remote,graphUri))
    })
    return models
}

function summarizeFunctionalComponents(moduleDefinition,req,sbol,remote,graphUri) {
    functionalComponents = []
    moduleDefinition.functionalComponents.forEach((functionalComponent) => {
	var functionalComponentResult = {}
	functionalComponent.link()
	functionalComponentResult = summarizeIdentified(functionalComponent,req)
	functionalComponentResult.definition = summarizeIdentified(functionalComponent.definition,req)
	functionalComponentResult.accessUrl = functionalComponent.access.toString().replace('http://sbols.org/v2#','http://sbolstandard.org/v2#')
	functionalComponentResult.accessStr = functionalComponent.access.toString().replace('http://sbols.org/v2#','')
	functionalComponentResult.directionUrl = functionalComponent.direction.toString().replace('http://sbols.org/v2#','http://sbolstandard.org/v2#')
	functionalComponentResult.directionStr = functionalComponent.direction.toString().replace('http://sbols.org/v2#','')
	functionalComponents.push(functionalComponentResult)
    })
    return functionalComponents
}

function summarizeInteractions(moduleDefinition,req) {
    interactions = []
    moduleDefinition.interactions.forEach((interaction) => {
	var interactionResult = {}
	interactionResult = summarizeIdentified(interaction,req)
	interactionResult.typeStr = ''
	interaction.types.forEach((type) => {
            var sboPrefix = 'http://identifiers.org/biomodels.sbo/'
	    if(type.toString().indexOf(sboPrefix) === 0) {
		var sboTerm = type.toString().slice(sboPrefix.length).split('_').join(':')
    		interactionResult.typeStr = systemsBiologyOntology[sboTerm].name
		interactionResult.typeURL = type.toString()
	    }
	})
	interactionResult.participations = []
	interaction.participations.forEach((participation) => {
	    participationResult = {}
	    participationResult = summarizeIdentified(participation,req)
	    participationResult.roleStr = ''
	    participation.roles.forEach((role) => {
		var sboPrefix = 'http://identifiers.org/biomodels.sbo/'
		if(role.toString().indexOf(sboPrefix) === 0) {
		    var sboTerm = role.toString().slice(sboPrefix.length).split('_').join(':')
    		    participationResult.roleStr = systemsBiologyOntology[sboTerm].name
		    participationResult.roleURL = role.toString()
		}
	    })
	    participationResult.participant = summarizeIdentified(participation.participant,req)
	    participationResult.participant.definition = summarizeIdentified(participation.participant.definition,req)
	    interactionResult.participations.push(participationResult)
	})
	interactions.push(interactionResult)
    })
    return interactions
}

module.exports = summarizeModuleDefinition





