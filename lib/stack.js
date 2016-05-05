
var StackFrontend = require('sbolstack-frontend')
var config = require('./config')

module.exports = function() {

   var backendUrl = config.get('backendURL')

   console.log('Creating stack frontend with backend URL ' + backendUrl)

   return new StackFrontend(
       backendUrl,
       config.get('backendUser'),
       config.get('backendPassword')
   )
}


