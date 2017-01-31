
var config = require('./lib/config')

var App = require('./lib/app')

var db = require('./lib/db')

//db.sequelize.sync({ force: true }).then(() => {

    var app = new App()

    app.listen(parseInt(config.get('port')))

//})



