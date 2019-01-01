var summarizeIdentified = require('./summarizeIdentified')
var summarizeParticipation = require('./summarizeParticipation')

var systemsBiologyOntology = require('../ontologies/systems-biology-ontology')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeInteraction(interaction,req,sbol,remote,graphUri) {

    if (interaction instanceof URI) {
	return uriToMeta(interaction)
    }

    var summary = {
	type: summarizeType(interaction),
	types: summarizeTypes(interaction),
	participations: summarizeParticipations(interaction,req,sbol,remote,graphUri)
    }

    summary = Object.assign(summary,summarizeIdentified(interaction,req,sbol,remote,graphUri))

    return summary
}

function summarizeType(interaction) {
    var typeResult = { typeStr: '',
		       typeURL: ''
		     }
    interaction.types.forEach((type) => {
        var sboPrefix = 'http://identifiers.org/biomodels.sbo/'
	if(type.toString().indexOf(sboPrefix) === 0) {
	    var sboTerm = type.toString().slice(sboPrefix.length).split('_').join(':')
    	    typeResult = { typeStr: systemsBiologyOntology[sboTerm].name,
			   typeURL: type.toString()
			 }
	}
    })
    return typeResult
}

function summarizeTypes(interaction) {
    var types = []
    interaction.types.forEach((type) => {
        var sboPrefix = 'http://identifiers.org/biomodels.sbo/'
	if(type.toString().indexOf(sboPrefix) === 0) {
	    var sboTerm = type.toString().slice(sboPrefix.length).split('_').join(':')
    	    typeResult = { uri: type.toString(),
			   term: type.toString(),
			   description: systemsBiologyOntology[sboTerm]
			 }
	} else {
    	    typeResult = { uri: type.toString()
			 }
	}
	types.push(typeResult)
    })
    return types
}

function summarizeParticipations(interaction,req,sbol,remote,graphUri) {
    var participations = []
    interaction.participations.forEach((participation) => {
	participations.push(summarizeParticipation(participation,req,sbol,remote,graphUri))
    })
    return participations
}

module.exports = summarizeInteraction





