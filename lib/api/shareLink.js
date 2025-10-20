const config = require('../config')
var getUrisFromReq = require('../getUrisFromReq')
const getOwnedBy = require('../query/ownedBy')
var util = require('../util')

async function shareLink (req, res) {
  const { uri, share } = getUrisFromReq(req, res)

  let graphUri = config.get('databasePrefix') + util.createTriplestoreID(req.params.userId)
  let ownedBy = await getOwnedBy(uri, graphUri)
  if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
    res.status(401).send('Not authorized to get a share link for this object')
  } else {
    res.status(200).type('text/plain')
    res.header('content-type', 'text/plain').send(share)
  }
}

module.exports = shareLink
