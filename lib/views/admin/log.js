const fs = require('fs')
const stripAnsiStream = require('strip-ansi-stream')

module.exports = function (req, res) {
  if (req.method == 'GET') {
    return getLog(req, res)
  } else {
    console.log('Unsupported method on log')
  }
}

function getLog (req, res) {
  var filename = 'synbiohub.log'
  var stat = fs.statSync(filename)
  var clean = stripAnsiStream()

  res.writeHead(200, {
    'Content-Type': 'text/plain',
    'Content-Length': stat.size
  })

  var readStream = fs.createReadStream(filename)
  readStream.pipe(clean).pipe(res)
}
