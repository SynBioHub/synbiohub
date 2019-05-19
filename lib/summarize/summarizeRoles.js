const lookupRole = require('../role')

function summarizeRoles (sbolObject) {
  return sbolObject.roles.map(uri => lookupRole(uri))
}

module.exports = summarizeRoles
