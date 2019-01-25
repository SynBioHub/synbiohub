
const pug = require('pug')

const config = require('../../config')

const spawn = require('child_process').spawn

module.exports = function (req, res) {
  const prefix = req.params.prefix

  restoreBackup(prefix).then((result) => {
    res.redirect('/admin/backup')
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}

function restoreBackup (prefix) {
  return new Promise((resolve, reject) => {
    const args = [
      prefix
    ]

    const process = spawn(__dirname + '/../../../scripts/restore_backup.sh', args)

    var output = []

    process.stdout.on('data', (data) => {
      console.log(data.toString())

      output.push(data)
    })

    process.stderr.on('data', (data) => {
      console.log(data.toString())
    })

    process.on('close', (exitCode) => {
      if (exitCode !== 0) {
        reject(new Error('restore_backup.sh returned exit code ' + exitCode))
        return
      }

      resolve(output.join(''))
    })
  })
}
