const execSync = require('child_process').execSync

module.exports = function () {
  const command = 'git rev-parse HEAD'

  try {
    var revision = execSync(command)
    return revision.toString().substring(0, 8)
  } catch (err) {
    console.log('Error trying to get revision')
    console.log(err)
    return ''
  }
}
