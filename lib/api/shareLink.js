
var getUrisFromReq = require('../getUrisFromReq')

function shareLink (req, res) {
  const { graphUri, uri, share } = getUrisFromReq(req, res)

  if (share && uri.startsWith(graphUri)) {
    res.status(200).type('text/plain')
    res.header('content-type', 'text/plain').send(share)
  } else {
    res.status(404).send(uri + ' not found')
  }
}

module.exports = shareLink
