
var namespace = require('./namespace')

var summarizeIdentified = require('./summarizeIdentified')
var summarizeComponent = require('./summarizeComponent')
var summarizeLocation = require('./summarizeLocation')
var summarizeRoles = require('./summarizeRoles')

var URI = require('sboljs').URI

function summarizeSequenceAnnotation(sequenceAnnotation,req,sbol,remote,graphUri) {

    if (sequenceAnnotation instanceof URI)
	return {
            uri: sequenceAnnotation + '',
	    id: sequenceAnnotation + ''
	}

    var definition
    
    if (sequenceAnnotation.component) {
	definition = summarizeComponent(sequenceAnnotation.component,req,sbol,remote,graphUri)
    }

    var summary = {
	component: definition,
	roles: summarizeRoles(sequenceAnnotation),
	locations: summarizeLocations(sequenceAnnotation,req,sbol,remote,graphUri)
    }

    summary = Object.assign(summary,summarizeIdentified(sequenceAnnotation,req,sbol,remote,graphUri))

    return summary
}

function summarizeLocations(sequenceAnnotation,req,sbol,remote,graphUri) {
    var locations = []
    sequenceAnnotation.locations.forEach((location) => {
	locations.push(summarizeLocation(location,req,sbol,remote,graphUri))
    })
    return locations
}

module.exports = summarizeSequenceAnnotation





