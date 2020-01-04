const pug = require('pug')
const extend = require('xtend')
const config = require('../config')
const getRegistries = require('../wor')
const validator = require('validator')
const createUser = require('../createUser')
const uuid = require('uuid/v4')
const theme = require('../theme')
const fs = require('fs')

module.exports = function (req, res) {
  if (config.get('firstLaunch') !== true) {
    res.status(500).send('Setup is already complete')
    return
  }

  var settings = {
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
    googleClientSecret: ''
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
    settings: settings,
    errors: [],
    ...locals
  }))
}

async function setupPost (req, res, settings) {
  let errors = []
  const updatedSettings = {
    ...settings,
    instanceName: trim(req.body.instanceName),
    instanceUrl: trim(req.body.instanceURL),
    uriPrefix: trim(req.body.uriPrefix),
    userName: trim(req.body.userName),
    affiliation: trim(req.body.affiliation),
    userFullName: trim(req.body.userFullName),
    userEmail: trim(req.body.userEmail),
    color: trim(req.body.color),
    userPassword: req.body.userPassword,
    userPasswordConfirm: req.body.userPasswordConfirm,
    frontPageText: trim(req.body.frontPageText),
    virtuosoINI: trim(req.body.virtuosoINI),
    virtuosoDB: trim(req.body.virtuosoDB),
    allowPublicSignup: !!req.body.allowPublicSignup,
    authProvider: trim(req.body.authProvider),
    googleClientId: trim(req.body.googleClientId),
    googleClientSecret: trim(req.body.googleClientSecret)
  }

  if (updatedSettings.instanceName === '') {
    errors.push('Please enter a name for your SynBioHub instance')
  }

  if (updatedSettings.userName === '') {
    errors.push('Please enter a username for the initial user account')
  }

  if (updatedSettings.userFullName === '') {
    errors.push('Please enter a full name for the initial user account')
  }

  if (!updatedSettings.userEmail || !validator.isEmail(updatedSettings.userEmail)) {
    errors.push('Please enter a valid e-mail address for the initial user account')
  }

  if (!updatedSettings.instanceUrl) {
    errors.push('Please enter the URL of your instance')
  }

  if (!updatedSettings.uriPrefix) {
    errors.push('Please enter the URI prefix of your instance')
  }

  if (updatedSettings.instanceUrl[updatedSettings.instanceUrl.length - 1] !== '/') {
    updatedSettings.instanceUrl += '/'
  }

  if (updatedSettings.uriPrefix[updatedSettings.uriPrefix.length - 1] !== '/') {
    updatedSettings.uriPrefix += '/'
  }

  if (!updatedSettings.frontPageText) {
    errors.push('Please enter some welcome text for your homepage')
  }

  if (updatedSettings.userPassword === '') {
    errors.push('Please enter a password for the initial user account')
  } else {
    if (updatedSettings.userPassword !== updatedSettings.userPasswordConfirm) {
      errors.push('Passwords did not match')
    }
  }

  if (!updatedSettings.virtuosoINI) {
    errors.push('Please enter your virtuoso INI location')
  }

  if (!updatedSettings.virtuosoDB) {
    errors.push('Please enter your virtuoso DB location')
  }

  errors = errors.concat(validateGoogleSettings(updatedSettings))

  if (errors.length > 0) {
    return setupForm(req, res, updatedSettings, {
      errors: errors
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

  config.set('triplestore', extend(config.get('triplestore'), {
    graphPrefix: updatedSettings.uriPrefix,
    defaultGraph: updatedSettings.uriPrefix + 'public',
    virtuosoINI: updatedSettings.virtuosoINI,
    virtuosoDB: updatedSettings.virtuosoDB
  }))

  config.set('externalAuth', {
    enabled: !!updatedSettings.authProvider,
    provider: updatedSettings.authProvider || null,
    google: {
      clientId: updatedSettings.googleClientId,
      clientSecret: updatedSettings.googleClientSecret
    }
  })

  try {
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
    errors.push('Could not create user')
    errors.push(err.stack)

    return setupForm(req, res, updatedSettings, {
      errors: errors
    })
  }
}

function trim (input) {
  return input ? input.trim() : ''
}

function validateGoogleSettings (settings) {
  const errors = []

  if (settings.authProvider !== 'google') return []

  if (!settings.googleClientId) {
    errors.push('Please enter Google Client ID')
  }

  if (!settings.googleClientSecret) {
    errors.push('Please enter Google Secret key')
  }

  return errors
}
