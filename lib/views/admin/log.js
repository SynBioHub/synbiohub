const config = require('../../config')
const escapeHtml = require('escape-html')
const fs = require('fs')
const moment = require('moment')
const pug = require('pug')

module.exports = function (req, res) {
  if (req.method === 'GET') {
    return render(req, res)
  } else {
    console.log('Unsupported method on log')
  }
}

function format (text) {
  let escaped = escapeHtml(text)
  let whitespace = escaped.match(/^\s*/g)

  return escaped.replace(/^\s*/g, '&nbsp;'.repeat(whitespace.length))
}

function getLog () {
  const logDir = 'logs'
  const date = moment().format('YYYY-MM-DD')
  const baseName = `synbiohub-${date}.debug`

  // Read all files in the log directory
  const files = fs.readdirSync(logDir)

  // Find all files for today (with or without suffix)
  const todayFiles = files
    .filter(f => f.startsWith(baseName))
    .map(f => {
      // Extract numeric suffix, if present
      const match = f.match(/\.debug(?:\.(\d+))?$/)
      return {
        name: f,
        suffix: match && match[1] ? parseInt(match[1], 10) : 0
      }
    })

  if (todayFiles.length === 0) {
    return []
  }

  // Find the file with the largest suffix
  const latestFile = todayFiles.reduce((a, b) => (a.suffix > b.suffix ? a : b)).name
  const filename = `${logDir}/${latestFile}`

  let file = fs.readFileSync(filename)
  let lines = file.toString().split(/(?:\r\n|\r|\n)/g)

  let log = []

  for (let line of lines) {
    let match = line.match(/^((?<level>debug|info|warn|error): )?(?<rest>.*)/)
    let logLine = format(match.groups.rest)

    if (match.groups.level) {
      log.push({ level: match.groups.level, line: logLine })
    } else {
      let last = log.pop()
      last.line += '<br>' + logLine
      log.push(last)
    }
  }

  return log
}

function render (req, res) {
  const locals = {
    config: config.get(),
    section: 'admin',
    adminSection: 'log',
    user: req.user,
    log: getLog()
  }
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'application/json').send(JSON.stringify(locals.log))
  } else {
    res.send(pug.renderFile('templates/views/admin/log.jade', locals))
  }
}
