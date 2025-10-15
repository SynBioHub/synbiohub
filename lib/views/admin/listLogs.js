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

  // Only include debug files
  const debugFiles = files.filter(f => f.match(/^synbiohub-\d{4}-\d{2}-\d{2}\.debug(\.\d+)?$/))

  // For each file, create an object with filename and title (title has .debug removed)
  const logs = debugFiles.map(f => ({
    filename: f,
    title: f.replace('.debug', '')
  }))

  return res.status(200).json({ logs })
}

module.exports = request
