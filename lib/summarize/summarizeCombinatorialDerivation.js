
var namespace = require('./namespace')
var summarizeTopLevel = require('./summarizeTopLevel')
var summarizeComponentDefinition = require('./summarizeComponentDefinition')
var summarizeVariableComponent = require('./summarizeVariableComponent')

var config = require('../config')

var URI = require('sboljs').URI

function summarizeCombinatorialDerivation(combinatorialDerivation,req,sbol,remote,graphUri) {

    if (combinatorialDerivation instanceof URI)
	return {
            uri: combinatorialDerivation + '',
	    id: combinatorialDerivation + ''
	}

    
    var template = summarizeComponentDefinition(combinatorialDerivation.template,req,sbol,remote,graphUri)
	
    var summary = {
        strategy: mapStrategy(combinatorialDerivation.strategy),
	template: template,
	displayList: template.displayList,
	variableComponents: summarizeVariableComponents(combinatorialDerivation,req,sbol,remote,graphUri)
    }
    return Object.assign(summary,summarizeTopLevel(combinatorialDerivation,req,sbol,remote,graphUri))
}

function summarizeVariableComponents(combinatorialDerivation,req,sbol,remote,graphUri) {
    variableComponents = []
    combinatorialDerivation.variableComponents.forEach((variableComponent) => {
	variableComponents.push(summarizeVariableComponent(variableComponent,req,sbol,remote,graphUri))
    })
    return variableComponents
}

function mapStrategy(strategy) {

    return { uri: strategy.toString(),
	     url: strategy.toString().replace('sbols.org','sbolstandard.org'),
	     name: strategy.toString().replace('http://sbols.org/v2#','')
	   }

}

module.exports = summarizeCombinatorialDerivation

