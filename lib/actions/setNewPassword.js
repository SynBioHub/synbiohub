const db = require('../db')

const { User } = db.model

module.exports = async function (req, res) {
  const token = req.body.token.trim()

  if (token.length === 0) {
    return res.status(400).send('invalid token')
  }

  const user = await User.findOne({
    where: {
      resetPasswordLink: token
    }
  })

  if (!user) {
    return res.status(400).send('bad token')
  }

  if (!req.body.password1) {
    return res.status(400).send('please enter a passord')
  }

  if (req.body.password1 !== req.body.password2) {
    return res.status(400).send('passwords do not match')
  }

  try {
    user.resetPasswordLink = ''
    user.password = User.hashPassword(req.body.password1)

    await user.save()
    req.session.users = [user.id]
    req.user = user

    req.session.user = user.id
    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'text/plain').send('New password set successfully')
    } else {
      res.redirect('/')
    }
  } catch (err) {
    res.status(500).send(err.stack)
  }
}
