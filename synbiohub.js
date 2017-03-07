
var config = require('./lib/config')

var App = require('./lib/app')

var db = require('./lib/db')

var fs = require('fs')

var jobUtils = require('./lib/jobs/job-utils')

var sliver = require('./lib/sliver')

var theme = require('./lib/theme')

var java = require('./lib/java')


if(!fs.existsSync('synbiohub.sqlite')) {

    db.sequelize.sync({ force: true }).then(startServer)

} else {

    startServer()

}

function startServer() {

    return initSliver()
                .then(() => java.init())
                .then(() => theme.setCurrentThemeFromConfig())
                .then(() => jobUtils.setRunningJobsToQueued())
                .then(() => jobUtils.resumeAllJobs())
                .then(() => {

        var app = new App()

        app.listen(parseInt(config.get('port')))
    })
}


function initSliver() {

    return new Promise((resolve, reject) => {

        // TODO
        resolve()


    })
}

process.on('SIGINT', function() {

    java.shutdown().then(() => process.exit())
    
})


