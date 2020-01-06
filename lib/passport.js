const passport = require('passport')
const { URL } = require('url')
const config = require('./config').get()

const registerGoogleStrategy = (app, passport, config) => {
  const { clientId, clientSecret, callbackUrl } = config
  const { OAuth2Strategy } = require('passport-google-oauth')

  passport.use(new OAuth2Strategy(
    {
      clientID: clientId,
      clientSecret,
      callbackURL: callbackUrl
    },
    (accessToken, refreshToken, profile, done) => {
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
    (req, res) => {
      res.json(req.user)
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
