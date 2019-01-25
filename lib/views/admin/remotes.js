
const pug = require('pug')

const sparql = require('../../sparql/sparql')

const db = require('../../db')

const config = require('../../config')

const { getSnapshots } = require('../../snapshots')

module.exports = function (req, res) {
  const remotesConfig = config.get('remotes')

  const remotes = Object.keys(remotesConfig).map((id) => remotesConfig[id])

  var locals = {
    config: config.get(),
    section: 'admin',
    adminSection: 'remotes',
    user: req.user,
    remotes: remotes,
    remoteTypes: ['ice', 'benchling']
  }

  res.send(pug.renderFile('templates/views/admin/remotes.jade', locals))
}
