const winston = require('winston')
require('winston-daily-rotate-file')

function createRotatingTransport (level) {
  let transport = new (winston.transports.DailyRotateFile)({
    filename: 'synbiohub-%DATE%.' + level,
    level: level,
    frequency: '24h',
    dirname: './logs',
    zippedArchive: true,
    maxFiles: '14d'
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
