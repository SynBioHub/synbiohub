const passport = require('passport')
const { URL } = require('url')
const cryptoRandomString = require('crypto-random-string')
const db = require('./db')
const config = require('./config').get()
const createUser = require('./createUser')
const { dotget } = require('./util')

const { User, UserExternalProfile } = db.model

const getProfileEmail = ({ emails }) => {
  const [{ value }] = emails
  return value
}

const getUsername = (profile) => {
  const email = getProfileEmail(profile)
  const [username] = email.split('@')

  return username
}

const findOrCreateUser = async (profile) => {
  const externalProfile = await UserExternalProfile.findOne({
    where: {
      profileName: profile.provider,
      profileId: profile.id
    },
    include: [User]
  })

  if (externalProfile) return externalProfile.user

  const user = await createUser({
    name: profile.displayName,
    username: getUsername(profile),
    email: getProfileEmail(profile),
    password: cryptoRandomString({ length: 48 }),
    affiliation: '',
    isAdmin: false,
    isCurator: false,
    isMember: true
  })

  await UserExternalProfile.createFromPassport(user.id, profile)

  return user
}

const registerGoogleStrategy = (app, passport, config) => {
  const { clientId, clientSecret, callbackUrl } = config
  const { OAuth2Strategy } = require('passport-google-oauth')

  passport.use(new OAuth2Strategy(
    {
      clientID: clientId,
      clientSecret,
      callbackURL: callbackUrl
    },
    async (accessToken, refreshToken, profile, done) => {
      // pass the profile to the auth callback
      done(null, profile)
    }
  ))

  const parsedCallbackUrl = new URL(callbackUrl)

  app.get('/auth/external', passport.authenticate('google', {
    scope: [
      'https://www.googleapis.com/auth/userinfo.profile',
      'https://www.googleapis.com/auth/userinfo.email'
    ]
  }))

  app.get(
    parsedCallbackUrl.pathname,
    passport.authenticate('google', { failureRedirect: '/' }),
    async (req, res) => {
      const { profile, user } = req

      // connect profile to existing user
      if (user) {
        await UserExternalProfile.createFromPassport(user.id, profile)
        return res.redirect('/profile')
      }

      try {
        const { id } = await findOrCreateUser(profile)

        req.session.user = id
        req.session.save(() => {
          res.redirect(req.body.next || '/')
        })
      } catch (error) {
        req.flash('login.error', error.message)
        res.redirect('/login')
      }
    }
  )
}

const registerStrategy = (app, passport) => {
  const strategy = dotget(config, 'externalAuth.provider')

  switch (strategy) {
    case 'google':
      return registerGoogleStrategy(app, passport, config.externalAuth.google)
  }
}

module.exports = (app) => {
  passport.serializeUser((user, done) => {
    done(null, user)
  })

  passport.deserializeUser((user, done) => {
    done(null, user)
  })

  app.use(passport.initialize({ userProperty: 'profile' }))
  registerStrategy(app, passport)
}
