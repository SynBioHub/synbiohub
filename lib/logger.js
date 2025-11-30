const winston = require('winston')
require('winston-daily-rotate-file')
const fs = require('fs')
const path = require('path')

function createRotatingTransport (level) {
  deleteOldLogFiles(path.resolve(__dirname, '../logs'))

  const transport = new (winston.transports.DailyRotateFile)({
    filename: 'synbiohub-%DATE%.' + level,
    level: level,
    frequency: '24h',
    maxSize: '1m',
    dirname: './logs',
    zippedArchive: true,
    maxFiles: '14d'
  })

  // Listen for the 'rotate' event and append a timestamp line to the rotated file
  transport.on('rotate', function (oldFilename, newFilename) {
    const timestamp = new Date().toISOString()
    const message = `--- Log rotated at ${timestamp} ---\n`
    // Append to the old file (the one that just got rotated)
    fs.appendFile(oldFilename, message, err => {
      if (err) console.error('Failed to append rotation timestamp:', err)
    })
  })

  return transport
}

const logger = winston.createLogger({
  level: 'debug',
  levels: {
    error: 0,
    warn: 1,
    info: 2,
    debug: 3
  },
  transports: [
    createRotatingTransport('debug'),
    createRotatingTransport('info'),
    createRotatingTransport('warn'),
    createRotatingTransport('error'),
    new winston.transports.Console()
  ],
  format: winston.format.simple()
})

module.exports = logger

function deleteOldLogFiles (logDir) {
  const files = fs.readdirSync(logDir)
  const now = new Date()

  files.forEach(file => {
    // Match files with a date in the format YYYY-MM-DD
    const match = file.match(/synbiohub-(\d{4}-\d{2}-\d{2})\..*/)
    if (match) {
      const fileDate = new Date(match[1])
      const diffDays = (now - fileDate) / (1000 * 60 * 60 * 24)
      if (diffDays > 14) {
        const filePath = path.join(logDir, file)
        fs.unlink(filePath, err => {
          if (err) {
            console.error(`Failed to delete old log file ${file}:`, err)
          } else {
            console.log(`Deleted old log file: ${file}`)
          }
        })
      }
    }
  })
}
