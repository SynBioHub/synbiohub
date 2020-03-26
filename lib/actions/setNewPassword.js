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

  try {
    user.resetPasswordLink = ''
    user.password = User.hashPassword(req.body.password1)

    await user.save()

    req.session.user = user.id
    res.redirect('/')
  } catch (err) {
    res.status(500).send(err.stack)
  }
}
