const privileges = require('../auth/privileges')
const access = require('../auth/access')

module.exports = function updateShare (req, res) {
  if (!privileges.canShare(req.privilege)) {
    res.sendStatus(403).end()
    return
  }

  let authId = req.body.authId
  let desiredPrivilege = req.body.newPrivilege

  access.update(authId, desiredPrivilege)
  res.sendStatus(200).end()
}
