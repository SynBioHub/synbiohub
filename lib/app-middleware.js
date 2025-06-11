const path = require('path')
const express = require('express')
const session = require('express-session')
const SequelizeStore = require('connect-sequelize')(session)
const flash = require('connect-flash')
const lessMiddleware = require('less-middleware')
const cookieParser = require('cookie-parser')
const bodyParser = require('body-parser')
const browserifyMiddleware = require('browserify-middleware')
const apiTokens = require('./apiTokens')
const db = require('./db')
const config = require('./config')
const initializePassport = require('./passport')
const cors = require('cors')

browserifyMiddleware.settings({
  mode: 'production',
  cache: '1 day',
  // debug: false,
  minify: true,
  precompile: true,
  transform: [[{ presets: ['@babel/preset-env', '@babel/preset-react'] }, 'babelify']]
})

module.exports = (app) => {
  app.get('/bundle.js', browserifyMiddleware(path.join(__dirname, '/../browser/synbiohub.js')))

  app.use(lessMiddleware('public'))
  app.use(express.static('public'))
  app.use(cookieParser())
  app.use(cors())
  app.use(
    session({
      secret: config.get('sessionSecret'),
      resave: false,
      saveUninitialized: false,
      store: new SequelizeStore(db.sequelize, {}, 'Session')
    })
  )
  app.use(flash())
  app.use(
    bodyParser.urlencoded({
      extended: true
    })
  )
  app.use(bodyParser.json())

  // Custom app middleware
  //

  // Append plainOrHtml() method to response object
  app.use(function (req, res, next) {
    res.plainOrHtml = function ({ status = 200, message }, htmlFn) {
      if (req.forceNoHTML || !req.accepts('text/html')) {
        return res.status(status).type('text/plain').send(message)
      }

      return htmlFn(req, res)
    }

    next()
  })

  app.use(function (req, res, next) {
    if (req.url !== '/setup' && req.url !== '/admin/theme' && config.get('firstLaunch') === true) {
      console.log('redirecting')

      res.redirect('/setup')
    } else {
      next()
    }
  })

  // Authenticate user
  app.use(async function (req, res, next) {
    const userID = req.session.user
      ? req.session.user
      : req.get('X-authorization')
        ? apiTokens.getUserIdFromToken(req.get('X-authorization'))
        : null

    req.user = await (userID &&
      db.model.User.findById(userID, {
        include: [db.model.UserExternalProfile]
      }))

    next()
  })

  // once everything is initialized we can setup passport
  initializePassport(app)
}
