const config = require('../config')
const db = require('../db')
const getUrisFromReq = require('../getUrisFromReq')
const pug = require('pug')

module.exports = async function (req, res, type) {
  let { uri } = getUrisFromReq(req, res)

  let shares = await db.model.Auth.findAll({
    where: {
      rootAuth: null,
      uri: uri
    },
    include: [{
      model: db.model.User,
      where: {
        virtual: false
      }
    }]
  })

  shares = shares.map(share => {
    share.removeUrl = config.get('instanceUrl') + 'share/' + share.id + '/delete'

    return share
  })

  let shareLinks = await db.model.Alias.findAll({
    where: {
      uri: uri
    }
  })

  shareLinks = shareLinks.map(shareLink => {
    shareLink.url = config.get('instanceUrl') + 'alias/' + shareLink.tag
    shareLink.removeUrl = config.get('instanceUrl') + 'alias/' + shareLink.tag + '/delete'
    return shareLink
  })

  var locals = {
    config: config.get(),
    user: req.user,
    shares: shares,
    shareLinks: shareLinks
  }

  res.send(pug.renderFile('templates/views/manageSharing.jade', locals))
}
