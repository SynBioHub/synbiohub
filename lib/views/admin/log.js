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
  var date = moment().format('YYYY-MM-DD')
  var filename = `logs/synbiohub-${date}.debug`

  if (!fs.existsSync(filename)) {
    return []
  }

  let file = fs.readFileSync(filename)
  let lines = file.toString()
    .replace(/[^\x00-\x7F]/g, '') // Filter out non-ASCII
    .split(/(?:\r\n|\r|\n)/g) // split into lines

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

  res.send(pug.renderFile('templates/views/admin/log.jade', locals))
}
