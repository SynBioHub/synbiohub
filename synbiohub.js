var config = require('./lib/config')
var App = require('./lib/app')
var db = require('./lib/db')
var fs = require('fs')
var jobUtils = require('./lib/jobs/job-utils')
var sliver = require('./lib/sliver')
var theme = require('./lib/theme')
var java = require('./lib/java')
var gitRev = require('./lib/gitRevision')


if(!fs.existsSync('synbiohub.sqlite') || fs.statSync('synbiohub.sqlite').size == 0) {
    db.sequelize.sync({ force: true }).then(startServer)
} else {
    db.umzug.up().then(() => {
        startServer()
    })
}

config.set('revision', gitRev())

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


