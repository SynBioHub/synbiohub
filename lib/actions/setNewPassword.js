const db = require('../db')

const { User } = db.model

function destroySession (req) {
  return new Promise((resolve) => {
    if (req.session && req.session.user !== undefined) {
      delete req.session.user
    }
    req.user = undefined
    if (req.session && typeof req.session.save === 'function') {
      req.session.save(() => resolve())
    } else {
      resolve()
    }
  })
}

module.exports = async function (req, res) {
  const token = (req.body.token || '').trim()

  if (!req.body.password1) {
    return res.status(400).send('please enter a password')
  }

  if (req.body.password1 !== req.body.password2) {
    return res.status(400).send('passwords do not match')
  }

  try {
    if (token.length > 0) {
      const user = await User.findOne({
        where: {
          resetPasswordLink: token
        }
      })

      if (!user) {
        return res.status(400).send('bad token')
      }

      user.resetPasswordLink = ''
      user.password = User.hashPassword(req.body.password1)
      await user.save()
      await destroySession(req)

      if (!req.accepts('text/html')) {
        return res.status(200).header('content-type', 'text/plain').send('New password set successfully')
      } else {
        return res.redirect('/login')
      }
    }

    if (!req.user) {
      return res.status(401).send('Login required')
    }

    const user = await User.findById(req.user.id)
    if (!user) {
      return res.status(404).send('user not found')
    }

    user.password = User.hashPassword(req.body.password1)
    await user.save()

    if (!req.accepts('text/html')) {
      return res.status(200).header('content-type', 'text/plain').send('New password set successfully')
    } else {
      return res.redirect('/profile')
    }
  } catch (err) {
    res.status(500).send(err.stack)
  }
}
