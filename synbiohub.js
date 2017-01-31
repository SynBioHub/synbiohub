
var config = require('./lib/config')

var App = require('./lib/app')

var app = new App()

app.listen(parseInt(config.get('port')))



