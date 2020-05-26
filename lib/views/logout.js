var apiTokens = require('../apiTokens')

module.exports = function (req, res) {

  if (req.session.users !== undefined) { delete req.session.users }
  if (req.session.user !== undefined) { delete req.session.user }

  if (req.get('X-authorization') !== undefined) {
    apiTokens.deleteToken(req.get('X-authorization'))
  }

  req.session.save(() => {
    if (!req.accepts('text/html')) {
      return res.status(200).send()
    } else {
      res.redirect('/')
    }
  })
}
