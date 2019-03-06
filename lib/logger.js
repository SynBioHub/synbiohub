const winston = require('winston')
const { format } = require('logform')
require('winston-daily-rotate-file')

function createRotatingTransport (level) {
  let transport = new (winston.transports.DailyRotateFile)({
    filename: 'synbiohub-%DATE%.' + level,
    level: level,
    frequency: '24h',
    dirname: './logs'
  })

  return transport
}

const cliFormat = format.cli()

const logger = winston.createLogger({
  level: 'info',
  transports: [
    createRotatingTransport('silly'),
    createRotatingTransport('debug'),
    createRotatingTransport('verbose'),
    createRotatingTransport('info'),
    createRotatingTransport('warn'),
    createRotatingTransport('error'),
    new winston.transports.Console()
  ],
  format: cliFormat
})

module.exports = logger
