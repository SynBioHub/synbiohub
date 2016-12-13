
var config = require('./lib/config')

var mongoose = require('mongoose')

mongoose.connect(config.get('databaseHost'), config.get('databaseName'))

var App = require('./lib/app')

var app = new App()

app.listen(parseInt(config.get('port')))



