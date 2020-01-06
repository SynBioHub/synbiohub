const path = require('path')
const express = require('express')
const session = require('express-session')
const SequelizeStore = require('connect-sequelize')(session)
const lessMiddleware = require('less-middleware')
const cookieParser = require('cookie-parser')
const bodyParser = require('body-parser')
const browserifyMiddleware = require('browserify-middleware')
const apiTokens = require('./apiTokens')
const db = require('./db')
const config = require('./config')
const initializePassport = require('./passport')

browserifyMiddleware.settings({
  mode: 'production',
  cache: '1 day',
  // debug: false,
  minify: true,
  precompile: true
})

module.exports = (app) => {
  app.get('/bundle.js', browserifyMiddleware(path.join(__dirname, '/../browser/synbiohub.js')))

  app.use(lessMiddleware('public'))
  app.use(express.static('public'))
  app.use(cookieParser())
  app.use(session({
    secret: config.get('sessionSecret'),
    resave: false,
    saveUninitialized: false,
    store: new SequelizeStore(db.sequelize, {}, 'Session')
  }))
  app.use(bodyParser.urlencoded({
    extended: true
  }))
  app.use(bodyParser.json())

  // Custom app middleware
  //

  // Append plainOrHtml() method to response object
  app.use(function (req, res, next) {
    res.plainOrHtml = function ({ status = 200, message }, htmlFn) {
      if (req.forceNoHTML || !req.accepts('text/html')) {
        return res.status(status)
          .type('text/plain')
          .send(message)
      }

      return htmlFn(req, res)
    }

    next()
  })

  app.use(function (req, res, next) {
    if (req.url !== '/setup' && config.get('firstLaunch') === true) {
      console.log('redirecting')

      res.redirect('/setup')
    } else {
      next()
    }
  })

  // Authenticate user
  app.use(async function (req, res, next) {
    const userID = req.session.user

    if (userID !== undefined) {
      req.user = await db.model.User.findById(userID)
    } else if (req.get('X-authorization') && req.get('X-authorization') !== '') {
      req.user = await apiTokens.getUserFromToken(req.get('X-authorization'))
    }

    next()
  })

  // once everything is initialized we can setup passport
  initializePassport(app)
}
