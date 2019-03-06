const winston = require('winston')
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

const logger = winston.createLogger({
  level: 'silly',
  transports: [
    createRotatingTransport('silly'),
    createRotatingTransport('debug'),
    createRotatingTransport('verbose'),
    createRotatingTransport('info'),
    createRotatingTransport('warn'),
    createRotatingTransport('error'),
    new winston.transports.Console()
  ]
})

module.export = logger
