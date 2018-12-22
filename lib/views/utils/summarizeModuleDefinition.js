
var namespace = require('./namespace')
var summarizeGenericTopLevel = require('./summarizeGenericTopLevel')

var getDisplayList = require('visbol/lib/getDisplayList').getDisplayList

var getInteractionList = require('visbol/lib/getInteractionList')

var URI = require('sboljs').URI

var config = require('../../config')

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
	models: summarizeModels(moduleDefinition,req),
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

    if (config.get('showModuleInteractions') && moduleDefinition.interactions.length > 0) {
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
	if (module.definition.uri) {
            moduleResult.defId = module.definition.displayId
            moduleResult.defName = module.definition.name
            if (module.definition.uri.toString().startsWith(config.get('databasePrefix'))) {
		moduleResult.url = '/' + module.definition.uri.toString().replace(config.get('databasePrefix'),'')
		if (module.definition.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
		    moduleResult.url += '/' + sha1('synbiohub_' + sha1(module.definition.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		}            
            } else {
		moduleResult.url = module.definition.uri.toString()
	    }
	} else {
            moduleResult.defId = module.definition.toString()
            moduleResult.defName = ''
            moduleResult.url = module.definition.toString()
	}
	modules.push(moduleResult)
    })
    return modules
}

function summarizeModels(moduleDefinition,req) {
    models = []
    moduleDefinition.models.forEach((model) => {
	var modelResult = {}
	if (model.uri) {
            if (model.uri.toString().startsWith(config.get('databasePrefix'))) {
		modelResult.url = '/' + model.uri.toString().replace(config.get('databasePrefix'),'')
		if (model.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
		    modelResult.url += '/' + sha1('synbiohub_' + sha1(model.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		}            
            } else {
		modelResult.url = model.uri.toString()
            }
            modelResult.version = model.uri.toString().substring(model.uri.toString().lastIndexOf('/')+1)
            var persId = model.uri.toString().substring(0,model.uri.toString().lastIndexOf('/'))
            modelResult.id = persId.substring(persId.lastIndexOf('/')+1)
	} else {
            modelResult.url = model.toString()
            modelResult.id = model.toString()
            modelResult.name = ''
	}
	models.push(modelResult)
    })
    return models
}

function summarizeFunctionalComponents(moduleDefinition,req) {
    functionalComponents = []
    moduleDefinition.functionalComponents.forEach((functionalComponent) => {
	var functionalComponentResult = {}
	functionalComponent.link()
	if (functionalComponent.definition.uri) {
            functionalComponentResult.defId = functionalComponent.definition.displayId
            functionalComponentResult.defName= functionalComponent.definition.name && functionalComponent.definition.name != '' ? functionalComponent.definition.name : functionalComponent.definition.displayId
            if (functionalComponent.definition.uri.toString().startsWith(config.get('databasePrefix'))) {
		functionalComponentResult.url = '/' + functionalComponent.definition.uri.toString().replace(config.get('databasePrefix'),'')
		if (functionalComponent.definition.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
		    functionalComponentResult.url += '/' + sha1('synbiohub_' + sha1(functionalComponent.definition.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		}  
            } else { 
		functionalComponentResult.url = functionalComponent.definition.uri.toString()
	    }
	} else {
            functionalComponentResult.defId = functionalComponent.definition.toString()
            functionalComponentResult.defName = ''
            functionalComponentResult.url = functionalComponent.definition.toString()
	}
	functionalComponentResult.typeStr = functionalComponent.access.toString().replace('http://sbols.org/v2#','') + ' '
            + functionalComponent.direction.toString().replace('http://sbols.org/v2#','').replace('none','')
	functionalComponents.push(functionalComponentResult)
    })
    return functionalComponents
}

function summarizeInteractions(moduleDefinition,req) {
    interactions = []
    moduleDefinition.interactions.forEach((interaction) => {
	var interactionResult = {}
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
	    participationResult.roleStr = ''
	    participation.roles.forEach((role) => {
		var sboPrefix = 'http://identifiers.org/biomodels.sbo/'
		if(role.toString().indexOf(sboPrefix) === 0) {
		    var sboTerm = role.toString().slice(sboPrefix.length).split('_').join(':')
    		    participationResult.roleStr = systemsBiologyOntology[sboTerm].name
		    participationResult.roleURL = role.toString()
		}
	    })
	    participationResult.participant = {}
	    if (participation.participant.definition.uri) {
		if (participation.participant.definition.uri.toString().startsWith(config.get('databasePrefix'))) {
		    participationResult.participant.url = '/' + participation.participant.uri.toString().replace(config.get('databasePrefix'),'')
		    if (participation.participant.definition.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
		    	participationResult.participant.url += '/' + sha1('synbiohub_' + sha1(participation.participant.definition.uri.toString()) + config.get('shareLinkSalt')) + '/share'

		    }            
		} else { 
		    participationResult.participant.url = participation.participant.definition.uri.toString()
		}
                participationResult.participant.defId = participation.participant.definition.displayId
                participationResult.participant.defName = participation.participant.definition.name?participation.participant.definition.name:participation.participant.definition.displayId
	    }else {
                participationResult.participant.defId = participation.participant.displayId
                participationResult.participant.defName = participation.participant.name?participation.participant.name:participation.participant.displayId
                participationResult.participant.url = participation.participant.definition.toString()
	    }
	    interactionResult.participations.push(participationResult)
	})
	interactions.push(interactionResult)
    })
    return interactions
}

module.exports = summarizeModuleDefinition





