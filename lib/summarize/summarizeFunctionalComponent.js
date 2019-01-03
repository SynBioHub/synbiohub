var summarizeComponentInstance = require('./summarizeComponentInstance')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeFunctionalComponent(functionalComponent,req,sbol,remote,graphUri) {

    if (functionalComponent instanceof URI) {
	return uriToMeta(functionalComponent)
    }
    var summary = {
	direction: { uri: functionalComponent.direction.toString(),
		     url: functionalComponent.direction.toString().replace('http://sbols.org/v2#','http://sbolstandard.org/v2#'),
		     id: functionalComponent.direction.toString().replace('http://sbols.org/v2#','')
		   }
    }

    summary = Object.assign(summary,summarizeComponentInstance(functionalComponent,req,sbol,remote,graphUri))

    return summary
}

module.exports = summarizeFunctionalComponent






