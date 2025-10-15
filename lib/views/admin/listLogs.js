const fs = require('fs')
const path = require('path')

function request (req, res) {
  const logDir = path.resolve(__dirname, '../../../logs')
  let files
  try {
    files = fs.readdirSync(logDir)
  } catch (err) {
    return res.status(500).send('Could not read log directory')
  }

  const logFiles = files.filter(f => f.match(/^synbiohub-\d{4}-\d{2}-\d{2}\..*/))

  return res.status(200).json({ logs: logFiles })
}

module.exports = { request }
