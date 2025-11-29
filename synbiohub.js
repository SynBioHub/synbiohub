const config = require('./lib/config')
const App = require('./lib/app')
const db = require('./lib/db')
const fs = require('fs')
const jobUtils = require('./lib/jobs/job-utils')
const java = require('./lib/java')
const logger = require('./lib/logger')
const gitRev = require('./lib/gitRevision')
const theme = require('./lib/theme')
const loggerOverride = require('./lib/loggerOverride')

loggerOverride()

// Log to error so that it shows up in all logfiles
console.error('SynBioHub server is restarting')

if (!fs.existsSync('synbiohub.sqlite') || fs.statSync('synbiohub.sqlite').size === 0) {
  db.sequelize.sync({ force: true }).then(startServer)
} else {
  db.umzug.up().then(startServer)
}

async function startServer () {
  await java.init()
  await theme.setCurrentThemeFromConfig()
  await jobUtils.setRunningJobsToQueued()
  await jobUtils.resumeAllJobs()

  const app = new App()
  app.listen(parseInt(config.get('port')))
}

process.on('SIGINT', function () {
  java.shutdown().then(() => process.exit())
})
