
var config = require('config')

var mongoose = require('mongoose')

mongoose.connect(config.get('databaseHost'), config.get('databaseName'))

var App = require('./lib/app')

var app = new App()

app.listen(7777)



