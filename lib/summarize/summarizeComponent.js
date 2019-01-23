var summarizeComponentInstance = require('./summarizeComponentInstance')
var summarizeRoles = require('./summarizeRoles')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeComponent (component, req, sbol, remote, graphUri) {
  if (component instanceof URI) {
    return uriToMeta(component)
  }
  var summary = {
    roles: summarizeRoles(component),
    roleIntegration: { uri: component.roleIntegration.toString(),
      url: component.roleIntegration.toString().replace('http://sbols.org/v2#', 'http://sbolstandard.org/v2#'),
      id: component.roleIntegration.toString().replace('http://sbols.org/v2#', '')
    }
  }

  summary = Object.assign(summary, summarizeComponentInstance(component, req, sbol, remote, graphUri))

  return summary
}

module.exports = summarizeComponent
