
var namespace = require('./namespace')
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
	modules: summarizeModules(moduleDefinition,req),
	roles: moduleDefinition.roles,
	models: summarizeModels(moduleDefinition,req,sbol,remote,graphUri),
	functionalComponents: summarizeFunctionalComponents(moduleDefinition,req),
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

function summarizeModules(moduleDefinition,req) {
    modules = []
    moduleDefinition.modules.forEach((module) => {
	var moduleResult = {}
	module.link()
	moduleResult.url = uriToUrl(module)
	moduleResult.defUrl = uriToUrl(module.definition)
	if (module.definition.uri) {
            moduleResult.defId = module.definition.displayId
            moduleResult.defName = module.definition.name
	} else {
            moduleResult.defId = module.definition.toString()
            moduleResult.defName = ''
	}
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

function summarizeFunctionalComponents(moduleDefinition,req) {
    functionalComponents = []
    moduleDefinition.functionalComponents.forEach((functionalComponent) => {
	var functionalComponentResult = {}
	functionalComponent.link()
	functionalComponentResult.url = uriToUrl(functionalComponent)
	functionalComponentResult.defUrl = uriToUrl(functionalComponent.definition)
	if (functionalComponent.definition.uri) {
            functionalComponentResult.defId = functionalComponent.definition.displayId
            functionalComponentResult.defName= functionalComponent.definition.name && functionalComponent.definition.name != '' ? functionalComponent.definition.name : functionalComponent.definition.displayId
	} else {
            functionalComponentResult.defId = functionalComponent.definition.toString()
            functionalComponentResult.defName = ''
	}
	functionalComponentResult.typeUrl = functionalComponent.access.toString().replace('http://sbols.org/v2#','http://sbolstandard.org/v2#')
	functionalComponentResult.typeStr = functionalComponent.access.toString().replace('http://sbols.org/v2#','')
	functionalComponentResult.dirUrl = functionalComponent.direction.toString().replace('http://sbols.org/v2#','http://sbolstandard.org/v2#')
	functionalComponentResult.dirStr = functionalComponent.direction.toString().replace('http://sbols.org/v2#','')
	functionalComponents.push(functionalComponentResult)
    })
    return functionalComponents
}

function summarizeInteractions(moduleDefinition,req) {
    interactions = []
    moduleDefinition.interactions.forEach((interaction) => {
	var interactionResult = {}
	interactionResult.url = uriToUrl(interaction)
	interactionResult.typeStr = ''
	interaction.types.forEach((type) => {
            var sboPrefix = 'http://identifiers.org/biomodels.sbo/'
	    if(type.toString().indexOf(sboPrefix) === 0) {
		var sboTerm = type.toString().slice(sboPrefix.length).split('_').join(':')
    		interactionResult.typeStr = systemsBiologyOntology[sboTerm].name
		interactionResult.typeURL = type.toString()
	    }
	})
	interactionResult.defId = interaction.displayId
	interactionResult.defName = interaction.name?interaction.name:interaction.displayId
	interactionResult.participations = []
	interaction.participations.forEach((participation) => {
	    participationResult = {}
	    participationResult.url = uriToUrl(participation)
	    participationResult.roleStr = ''
	    participation.roles.forEach((role) => {
		var sboPrefix = 'http://identifiers.org/biomodels.sbo/'
		if(role.toString().indexOf(sboPrefix) === 0) {
		    var sboTerm = role.toString().slice(sboPrefix.length).split('_').join(':')
    		    participationResult.roleStr = systemsBiologyOntology[sboTerm].name
		    participationResult.roleURL = role.toString()
		}
	    })
	    participationResult.defUrl = uriToUrl(participation.participant.definition)
	    if (participation.participant.definition.uri) {
                participationResult.defId = participation.participant.definition.displayId
                participationResult.defName = participation.participant.definition.name?participation.participant.definition.name:participation.participant.definition.displayId
	    }else {
                participationResult.defId = participation.participant.displayId
                participationResult.defName = participation.participant.name?participation.participant.name:participation.participant.displayId
	    }
	    interactionResult.participations.push(participationResult)
	})
	interactions.push(interactionResult)
    })
    return interactions
}

module.exports = summarizeModuleDefinition





