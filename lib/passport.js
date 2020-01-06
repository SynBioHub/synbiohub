const passport = require('passport')
const { URL } = require('url')
const cryptoRandomString = require('crypto-random-string')
const db = require('./db')
const config = require('./config').get()
const createUser = require('./createUser')

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
  const externalProfileFields = {
    profileName: profile.provider,
    profileId: profile.id
  }
  const externalProfile = await UserExternalProfile.findOne({
    where: externalProfileFields,
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

  await UserExternalProfile.create({
    userId: user.id,
    ...externalProfileFields
  })

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
      try {
        const user = await findOrCreateUser(profile)
        done(null, user)
      } catch (error) {
        done(error, null)
      }
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
    (req, res) => {
      const { user } = req

      req.session.user = user.id
      req.session.save(() => {
        res.redirect(req.body.next || '/')
      })
    }
  )
}

const registerStrategy = (app, passport) => {
  const strategy = config.externalAuth.provider

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

  app.use(passport.initialize())
  registerStrategy(app, passport)
}
