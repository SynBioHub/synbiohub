var summarizeIdentified = require('./summarizeIdentified')
var summarizeMapsTo = require('./summarizeMapsTo')

var URI = require('sboljs').URI

function summarizeComponentInstance(componentInstance,req,sbol,remote,graphUri) {

    if (componentInstance instanceof URI)
	return {
            uri: componentInstance + '',
	    id: componentInstance + ''
	}
    var summarizeComponentDefinition = require('./summarizeComponentDefinition')

    var definition = summarizeComponentDefinition(componentInstance.definition,req,sbol,remote,graphUri)

    var summary = {
	definition: definition,
	displayList: definition.displayList,
	access: { uri: componentInstance.access.toString(),
		  url: componentInstance.access.toString().replace('http://sbols.org/v2#','http://sbolstandard.org/v2#'),
		  id: componentInstance.access.toString().replace('http://sbols.org/v2#','')
		},
	mapsTos: summarizeMapsTos(componentInstance,req,sbol,remote,graphUri)
    }

    summary = Object.assign(summary,summarizeIdentified(componentInstance,req,sbol,remote,graphUri))

    return summary
}

function summarizeMapsTos(componentInstance,req,sbol,remote,graphUri) {
    var mapsTos = []
    componentInstance.mappings.forEach((mapsTo) => {
	mapsTos.push(summarizeMapsTo(mapsTo,req,sbol,remote,graphUri))
    })
    return mapsTos
}

module.exports = summarizeComponentInstance





