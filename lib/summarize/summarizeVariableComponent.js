
var namespace = require('./namespace')
var summarizeIdentified = require('./summarizeIdentified')
var summarizeComponentDefinition = require('./summarizeComponentDefinition')
var summarizeComponent = require('./summarizeComponent')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeVariableComponent(variableComponent,req,sbol,remote,graphUri) {
    if (variableComponent instanceof URI) {
	return uriToMeta(variableComponent)
    }	
    var summary = {
        operator: mapOperator(variableComponent.operator),
	variable: summarizeComponent(variableComponent.variable,req,sbol,remote,graphUri),
	variants: summarizeVariants(variableComponent,req,sbol,remote,graphUri)
    }
    return Object.assign(summary,summarizeIdentified(variableComponent,req,sbol,remote,graphUri))
}

function summarizeVariants(variableComponent,req,sbol,remote,graphUri) {
    var variants = []
    variableComponent.variants.forEach((variant) => {
	variantResult = summarizeIdentified(variant,req,sbol,remote,graphUri)
	variants.push(variantResult)
    })
    variableComponent.variantCollections.forEach((variant) => {
	variantResult = summarizeIdentified(variant,req,sbol,remote,graphUri)
	variants.push(variantResult)
    })
    variableComponent.variantDerivations.forEach((variant) => {
	variantResult = summarizeIdentified(variant,req,sbol,remote,graphUri)
	variants.push(variantResult)
    })
    return variants
}

function mapOperator(operator) {

    return { uri: operator.toString(),
	     url: operator.toString().replace('sbols.org','sbolstandard.org'),
	     name: operator.toString().replace('http://sbols.org/v2#','')
	   }

}

module.exports = summarizeVariableComponent

