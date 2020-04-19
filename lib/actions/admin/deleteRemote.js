
const config = require('../../config')

module.exports = function (req, res) {
  const remoteId = req.body.id
  var remotes = config.get('remotes')

  if (!remoteId || !remotes || !remotes[remoteId]) {
    return res.status(400).header('content-type', 'text/plain').send('Must provide a valid remote id')
  }

  delete remotes[remoteId]

  config.set('remotes', remotes)

  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'text/plain').send('Remote (' + remoteId + ') deleted successfully')
  } else {
    res.redirect('/admin/remotes')
  }
}
