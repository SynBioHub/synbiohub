
var namespace = require('./namespace')
var summarizeIdentified = require('./summarizeIdentified')
var summarizeGenericTopLevel = require('./summarizeGenericTopLevel')
var summarizeComponentDefinition = require('./summarizeComponentDefinition')

var config = require('../../config')

var URI = require('sboljs').URI

function summarizeCombinatorialDerivation(combinatorialDerivation,req,sbol,remote,graphUri) {

    if (combinatorialDerivation instanceof URI)
	return {
            uri: combinatorialDerivation + '',
	    id: combinatorialDerivation + ''
	}

    
    
    var summary = {
        strategy: mapStrategy(combinatorialDerivation.strategy),
	displayList: summarizeComponentDefinition(combinatorialDerivation.template,req,sbol,remote,graphUri).displayList,
	variableComponents: summarizeVariableComponents(combinatorialDerivation,req)
    }
    return Object.assign(summary,summarizeGenericTopLevel(combinatorialDerivation,req,sbol,remote,graphUri))
}

function summarizeVariableComponents(combinatorialDerivation,req) {
    variableComponents = []
    combinatorialDerivation.variableComponents.forEach((variableComponent) => {
	variableComponentResult = {}
	variableComponent.link()
	variableComponentResult = summarizeIdentified(variableComponent,req)
	variableComponentResult.operator = mapOperator(variableComponent.operator)
	variableComponentResult.variable = summarizeIdentified(variableComponent.variable,req)
	variableComponentResult.variants = []
	variableComponent.variants.forEach((variant) => {
	    variantResult = summarizeIdentified(variant,req)
	    variableComponentResult.variants.push(variantResult)
	})
	variableComponent.variantCollections.forEach((variant) => {
	    variantResult = summarizeIdentified(variant,req)
	    variableComponentResult.variants.push(variantResult)
	})
	variableComponent.variantDerivations.forEach((variant) => {
	    variantResult = summarizeIdentified(variant,req)
	    variableComponentResult.variants.push(variantResult)
	})
	variableComponents.push(variableComponentResult)				     
    })
    return variableComponents
}

function mapStrategy(strategy) {

    return { uri: strategy.toString(),
	     url: strategy.toString().replace('sbols.org','sbolstandard.org'),
	     name: strategy.toString().replace('http://sbols.org/v2#','')
	   }

}


function mapOperator(operator) {

    return { uri: operator.toString(),
	     url: operator.toString().replace('sbols.org','sbolstandard.org'),
	     name: operator.toString().replace('http://sbols.org/v2#','')
	   }

}

module.exports = summarizeCombinatorialDerivation

