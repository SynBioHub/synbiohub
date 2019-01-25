var summarizeIdentified = require('./summarizeIdentified')
var summarizeFunctionalComponent = require('./summarizeFunctionalComponent')

var systemsBiologyOntology = require('../ontologies/systems-biology-ontology')

var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeParticipation (participation, req, sbol, remote, graphUri) {
  if (participation instanceof URI) {
    return uriToMeta(participation)
  }

  var summary = {
    role: summarizeSBORole(participation),
    roles: summarizeSBORoles(participation),
    participant: summarizeFunctionalComponent(participation.participant, req, sbol, remote, graphUri)
  }

  summary = Object.assign(summary, summarizeIdentified(participation, req, sbol, remote, graphUri))

  return summary
}

function summarizeSBORole (participation) {
  var roleResult = { roleStr: '',
    roleURL: ''
  }
  participation.roles.forEach((role) => {
    var sboPrefix = 'http://identifiers.org/biomodels.sbo/'
    if (role.toString().indexOf(sboPrefix) === 0) {
      var sboTerm = role.toString().slice(sboPrefix.length).split('_').join(':')
      roleResult = { roleStr: systemsBiologyOntology[sboTerm].name,
        roleURL: role.toString()
      }
    }
  })
  return roleResult
}

function summarizeSBORoles (participation) {
  var roles = []
  participation.roles.forEach((role) => {
    var sboPrefix = 'http://identifiers.org/biomodels.sbo/'
    if (role.toString().indexOf(sboPrefix) === 0) {
      var sboTerm = role.toString().slice(sboPrefix.length).split('_').join(':')
      roleResult = { uri: role.toString(),
        term: role.toString(),
        description: systemsBiologyOntology[sboTerm]
      }
    }
    roles.push(roleResult)
  })
  return roles
}

module.exports = summarizeParticipation
