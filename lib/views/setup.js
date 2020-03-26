const pug = require('pug')
const uuid = require('uuid/v4')
const fs = require('fs')
const Joi = require('joi')
const config = require('../config')
const getRegistries = require('../wor')
const createUser = require('../createUser')
const theme = require('../theme')
const passportUtil = require('../passport/util')

const requiredString = (errorMessage, { trim = true } = {}) => Joi.string()
  .required()
  .trim(trim)
  .error(() => errorMessage)

const settingsSchema = Joi.object({
  instanceName: requiredString('Please enter a name for your SynBioHub instance'),
  userName: requiredString('Please enter a username for the initial user account'),
  userFullName: requiredString('Please enter a full name for the initial user account'),
  userEmail: requiredString('Please enter a valid e-mail address for the initial user account').email(),
  instanceUrl: requiredString('Please enter the URL of your instance'),
  uriPrefix: requiredString('Please enter the URI prefix of your instance'),
  frontPageText: requiredString('Please enter some welcome text for your homepage'),
  userPassword: requiredString('Please enter a password for the initial user account', { trim: false }),
  userPasswordConfirm: requiredString('Passwords didn\'t match', { trim: false })
    .valid(Joi.ref('userPassword')),
  virtuosoINI: requiredString('Please enter your virtuoso INI location'),
  virtuosoDB: requiredString('Please enter your virtuoso DB location'),
  googleClientId: Joi.when('authProvider', {
    is: 'google',
    then: requiredString('Please enter your Google Client ID')
  }),
  googleClientSecret: Joi.when('authProvider', {
    is: 'google',
    then: requiredString('Please enter your Google Client Secret')
  }),
  googleCallbackUrl: Joi.when('authProvider', {
    is: 'google',
    then: requiredString('Please enter your redirect url for Google OAuth')
      .uri({
        scheme: ['http', 'https']
      })
  }),
  affiliation: Joi.string().allow(''),
  color: Joi.string().trim().allow(''),
  allowPublicSignup: Joi.bool(),
  authProvider: Joi.string().allow('')
})

module.exports = function (req, res) {
  if (config.get('firstLaunch') !== true) {
    res.status(409).send('Setup is already complete')
    return
  }

  const settings = {
    instanceName: 'My SynBioHub',
    instanceUrl: req.protocol + '://' + req.get('Host') + '/',
    uriPrefix: req.protocol + '://' + req.get('Host') + '/',
    userName: '',
    affiliation: '',
    userFullName: '',
    userEmail: '',
    color: '#D25627',
    authProvider: '',
    googleClientId: '',
    googleClientSecret: '',
    googleCallbackUrl: ''
  }

  if (req.method === 'POST') {
    setupPost(req, res, settings)
  } else {
    setupForm(req, res, settings, {})
  }
}

function setupForm (req, res, settings, locals) {
  res.send(pug.renderFile('templates/views/setup.jade', {
    config: config.get(),
    title: 'First Time Setup - SynBioHub',
    errors: [],
    settings,
    ...locals
  }))
}

async function setupPost (req, res, settings) {
  const { error, value: updatedSettings } = settingsSchema.validate(
    {
      ...settings,
      instanceName: req.body.instanceName,
      instanceUrl: req.body.instanceURL,
      uriPrefix: req.body.uriPrefix,
      userName: req.body.userName,
      affiliation: req.body.affiliation,
      userFullName: req.body.userFullName,
      userEmail: req.body.userEmail,
      color: req.body.color,
      userPassword: req.body.userPassword,
      userPasswordConfirm: req.body.userPasswordConfirm,
      frontPageText: req.body.frontPageText,
      virtuosoINI: req.body.virtuosoINI,
      virtuosoDB: req.body.virtuosoDB,
      allowPublicSignup: !!req.body.allowPublicSignup,
      authProvider: req.body.authProvider,
      googleClientId: req.body.googleClientId,
      googleClientSecret: req.body.googleClientSecret,
      googleCallbackUrl: req.body.googleCallbackUrl
    },
    { abortEarly: false }
  )

  if (updatedSettings.instanceUrl[updatedSettings.instanceUrl.length - 1] !== '/') {
    updatedSettings.instanceUrl += '/'
  }

  if (updatedSettings.uriPrefix[updatedSettings.uriPrefix.length - 1] !== '/') {
    updatedSettings.uriPrefix += '/'
  }

  if (error) {
    return setupForm(req, res, updatedSettings, {
      errors: mapJoiErrors(error)
    })
  }

  if (req.file) {
    const logoFilename = 'logo_uploaded.' + req.file.originalname.split('.')[1]
    fs.writeFileSync('public/' + logoFilename, req.file.buffer)

    config.set('instanceLogo', '/' + logoFilename)
  }

  config.set('instanceName', updatedSettings.instanceName)
  config.set('sessionSecret', uuid())
  config.set('shareLinkSalt', uuid())
  config.set('passwordSalt', uuid())
  config.set('instanceUrl', updatedSettings.instanceUrl)
  config.set('webOfRegistries', { [updatedSettings.uriPrefix.slice(0, -1)]: updatedSettings.instanceUrl.slice(0, -1) })
  config.set('databasePrefix', updatedSettings.instanceUrl)
  config.set('themeParameters', { 'default': { baseColor: updatedSettings.color } })
  config.set('frontPageText', updatedSettings.frontPageText)
  config.set('allowPublicSignup', updatedSettings.allowPublicSignup)
  config.set('databasePrefix', updatedSettings.uriPrefix)

  config.set('triplestore', {
    ...config.get('triplestore'),
    graphPrefix: updatedSettings.uriPrefix,
    defaultGraph: updatedSettings.uriPrefix + 'public',
    virtuosoINI: updatedSettings.virtuosoINI,
    virtuosoDB: updatedSettings.virtuosoDB
  })

  config.set('externalAuth', {
    enabled: !!updatedSettings.authProvider,
    provider: updatedSettings.authProvider || null,
    google: {
      clientId: updatedSettings.googleClientId,
      clientSecret: updatedSettings.googleClientSecret,
      callbackUrl: updatedSettings.googleCallbackUrl
    }
  })

  try {
    await passportUtil.installDependencies(updatedSettings.authProvider)

    const user = await createUser({
      username: updatedSettings.userName,
      name: updatedSettings.userFullName,
      email: updatedSettings.userEmail,
      affiliation: updatedSettings.affiliation || '',
      password: updatedSettings.userPassword,
      isAdmin: true,
      isCurator: true,
      isMember: true
    })

    config.set('firstLaunch', false)

    await theme.setCurrentThemeFromConfig()
    await getRegistries()

    req.session.user = user.id
    req.session.save(() => {
      res.redirect(req.body.next || '/')
    })
  } catch (err) {
    return setupForm(req, res, updatedSettings, {
      errors: [
        'Could not create user',
        err.stack
      ]
    })
  }
}

function mapJoiErrors ({ details }) {
  const mapped = details.map(({ message }) => message)

  // Joi might duplicate error for each violated rule
  // this will make sure to return only unique messages
  return [...new Set(mapped)]
}
