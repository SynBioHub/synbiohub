
var namespace = require('./namespace')
var summarizeIdentified = require('./summarizeIdentified')
var summarizeFunctionalComponent = require('./summarizeFunctionalComponent')
var summarizeComponentDefinition = require('./summarizeComponentDefinition')
var summarizeTopLevel = require('./summarizeTopLevel')
var summarizeModel = require('./summarizeModel')
var summarizeModule = require('./summarizeModule')
var summarizeInteraction = require('./summarizeInteraction')
var summarizeRoles = require('./summarizeRoles')

var getDisplayList = require('visbol/lib/getDisplayList').getDisplayList

var getInteractionList = require('visbol/lib/getInteractionList')

var config = require('../config')

var uriToUrl = require('../uriToUrl')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

var systemsBiologyOntology = require('../ontologies/systems-biology-ontology')

function summarizeModuleDefinition(moduleDefinition,req,sbol,remote,graphUri) {
    
    if (moduleDefinition instanceof URI) {
	return uriToMeta(moduleDefinition)
    }

    var summary = {
        numSubModules: moduleDefinition.modules.length,
        numSubModulesTotal: 0,
	displayList: createDisplayList(moduleDefinition,req),
	modules: summarizeModules(moduleDefinition,req,sbol,remote,graphUri),
	roles: summarizeRoles(moduleDefinition),
	models: summarizeModels(moduleDefinition,req,sbol,remote,graphUri),
	functionalComponents: summarizeFunctionalComponents(moduleDefinition,req,sbol,remote,graphUri),
	interactions: summarizeInteractions(moduleDefinition,req,sbol,remote,graphUri)
    }

    summary = Object.assign(summary,summarizeTopLevel(moduleDefinition,req,sbol,remote,graphUri))

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
    var modules = []
    moduleDefinition.modules.forEach((module) => {
	modules.push(summarizeModule(module,req,sbol,remote,graphUri))
    })
    return modules
}

function summarizeModels(moduleDefinition,req,sbol,remote,graphUri) {
    var models = []
    moduleDefinition.models.forEach((model) => {
	models.push(summarizeModel(model,req,sbol,remote,graphUri))
    })
    return models
}

function summarizeFunctionalComponents(moduleDefinition,req,sbol,remote,graphUri) {
    var functionalComponents = []
    moduleDefinition.functionalComponents.forEach((functionalComponent) => {
	functionalComponents.push(summarizeFunctionalComponent(functionalComponent,req,sbol,remote,graphUri))
    })
    return functionalComponents
}

function summarizeInteractions(moduleDefinition,req,sbol,remote,graphUri) {
    var interactions = []
    moduleDefinition.interactions.forEach((interaction) => {
	interactions.push(summarizeInteraction(interaction,req,sbol,remote,graphUri))
    })
    return interactions
}

module.exports = summarizeModuleDefinition





