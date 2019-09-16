const alias = require('../auth/alias')
const virtualUser = require('../auth/virtualUser')

module.exports = async function (req, res) {
  let tag = req.params.tag
  let user = await alias.remove(tag)
  virtualUser.remove(user)

  res.redirect(req.header('Referer')) // go back
}
