const pug = require('pug')
const config = require('../../config')
const theme = require('../../theme')
const updateLogo = require('../../actions/admin/updateLogo')
const updateWor = require('../../actions/admin/updateWor')
const extend = require('xtend')
const loggerOverride = require('../../loggerOverride')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    if (req.body.showModuleInteractions &&
      (req.body.showModuleInteractions === 'on' || req.body.showModuleInteractions === 'true')) {
      config.set('showModuleInteractions', true)
    } else if (req.body.showModuleInteractions &&
      (req.body.showModuleInteractions === 'off' || req.body.showModuleInteractions === 'false')) {
      config.set('showModuleInteractions', false)
    } else if (req.body.showModuleInteractions === undefined) {
      config.set('showModuleInteractions', false)
    } else {
      config.set('showModuleInteractions', true) // default state
    }

    if (req.body.removePublicEnabled &&
      (req.body.removePublicEnabled === 'on' || req.body.removePublicEnabled === 'true')) {
      config.set('removePublicEnabled', true)
    } else if (req.body.removePublicEnabled &&
      (req.body.removePublicEnabled === 'off' || req.body.removePublicEnabled === 'false')) {
      config.set('removePublicEnabled', false)
    } else if (req.body.removePublicEnabled === undefined) {
      config.set('removePublicEnabled', false)
    } else {
      config.set('removePublicEnabled', true) // default state
    }

    if (req.body.requireLogin &&
      (req.body.requireLogin === 'on' || req.body.requireLogin === 'true')) {
      config.set('requireLogin', true)
    } else if (req.body.requireLogin &&
      (req.body.requireLogin === 'off' || req.body.requireLogin === 'false')) {
      config.set('requireLogin', false)
    } else if (req.body.requireLogin === undefined) {
      config.set('requireLogin', false)
    } else {
      config.set('requireLogin', false) // default state
    }

    if (req.body.suppressInfoLogs &&
      (req.body.suppressInfoLogs === 'on' || req.body.suppressInfoLogs === 'true')) {
      config.set('suppressInfoLogs', true)
    } else if (req.body.suppressInfoLogs &&
      (req.body.suppressInfoLogs === 'off' || req.body.suppressInfoLogs === 'false')) {
      config.set('suppressInfoLogs', false)
    } else if (req.body.suppressInfoLogs === undefined) {
      config.set('suppressInfoLogs', false)
    } else {
      config.set('suppressInfoLogs', false) // default state
    }

    if (req.body.suppressDebugLogs &&
      (req.body.suppressDebugLogs === 'on' || req.body.suppressDebugLogs === 'true')) {
      config.set('suppressDebugLogs', true)
    } else if (req.body.suppressDebugLogs &&
      (req.body.suppressDebugLogs === 'off' || req.body.suppressDebugLogs === 'false')) {
      config.set('suppressDebugLogs', false)
    } else if (req.body.suppressDebugLogs === undefined) {
      config.set('suppressDebugLogs', false)
    } else {
      config.set('suppressDebugLogs', false) // default state
    }

    if (req.body.suppressWarningLogs &&
      (req.body.suppressWarningLogs === 'on' || req.body.suppressWarningLogs === 'true')) {
      config.set('suppressWarningLogs', true)
    } else if (req.body.suppressWarningLogs &&
      (req.body.suppressWarningLogs === 'off' || req.body.suppressWarningLogs === 'false')) {
      config.set('suppressWarningLogs', false)
    } else if (req.body.suppressWarningLogs === undefined) {
      config.set('suppressWarningLogs', false)
    } else {
      config.set('suppressWarningLogs', false) // default state
    }

    if (req.body.suppressErrorLogs &&
      (req.body.suppressErrorLogs === 'on' || req.body.suppressErrorLogs === 'true')) {
      config.set('suppressErrorLogs', true)
    } else if (req.body.suppressErrorLogs &&
      (req.body.suppressErrorLogs === 'off' || req.body.suppressErrorLogs === 'false')) {
      config.set('suppressErrorLogs', false)
    } else if (req.body.suppressErrorLogs === undefined) {
      config.set('suppressErrorLogs', false)
    } else {
      config.set('suppressErrorLogs', false) // default state
    }

    loggerOverride()

    post(req, res)
  } else {
    form(req, res)
  }
}

function form (req, res) {
  const currentTheme = theme.getCurrentTheme()

  const themeParameters = currentTheme.parameters.map((parameter) => {
    return {
      name: parameter.name,
      variable: parameter.variable,
      value: theme.getParameterValue(parameter)
    }
  })

  var locals = {
    instanceName: config.get('instanceName'),
    frontendURL: config.get('frontendURL'),
    instanceUrl: config.get('instanceUrl'),
    uriPrefix: config.get('databasePrefix'),
    frontPageText: config.get('frontPageText'),
    firstLaunch: config.get('firstLaunch'),
    altHome: config.get('altHome'),
    showModuleInteractions: config.get('showModuleInteractions'),
    removePublicEnabled: config.get('removePublicEnabled'),
    allowPublicSignup: config.get('allowPublicSignup'),
    requireLogin: config.get('requireLogin'),
    suppressInfoLogs: config.get('suppressInfoLogs'),
    suppressDebugLogs: config.get('suppressDebugLogs'),
    suppressWarningLogs: config.get('suppressWarningLogs'),
    suppressErrorLogs: config.get('suppressErrorLogs'),
    currentTheme: currentTheme,
    themeParameters: themeParameters,
    pluginsUseLocalCompose: config.get('pluginsUseLocalCompose'),
    pluginLocalComposePrefix: config.get('pluginLocalComposePrefix')
  }
  if (!req.accepts('text/html')) {
    return res.status(200).header('content-type', 'application/json').send(JSON.stringify(locals))
  } else {
    locals = extend(
      {
        config: config.get(),
        section: 'admin',
        adminSection: 'theme',
        user: req.user
      },
      locals
    )
    res.send(pug.renderFile('templates/views/admin/theme.jade', locals))
  }
}

function post (req, res) {
  const currentTheme = theme.getCurrentTheme()

  const newParameters = {}

  currentTheme.parameters.map((parameter) => {
    var newParameter = req.body[parameter.variable]

    if (newParameter !== undefined) {
      if (newParameter !== parameter['default']) {
        if (parameter.variable === 'baseColor') {
          if (!newParameter.startsWith('#')) {
            newParameter = '#' + newParameter
          }
          var pat = /#[0123456789abcdef]{6}/i
          if (newParameter.length === 7 && pat.test(newParameter)) {
            console.log('match=' + newParameter)
            newParameters[parameter.variable] = newParameter
          }
        } else {
          newParameters[parameter.variable] = newParameter
        }
      }
    }
  })

  const localConfig = config.getLocal()

  const themeName = theme.getCurrentThemeName()

  if (localConfig.themeParameters === undefined) {
    localConfig.themeParameters = {}
  }

  localConfig.themeParameters[themeName] = newParameters

  config.set(localConfig)

  let publishUpdate = false

  console.log(JSON.stringify(req.body))

  if (req.body.frontPageText && req.body.frontPageText !== config.get('frontPageText')) {
    config.set('frontPageText', req.body.frontPageText)
    publishUpdate = true
  }

  if (req.body.instanceName && req.body.instanceName !== config.get('instanceName')) {
    config.set('instanceName', req.body.instanceName)
    publishUpdate = true
  }

  console.log(typeof (req.body))

  if (typeof req.body === 'object' && 'altHome' in req.body && req.body.altHome !== config.get('altHome')) {
    config.set('altHome', req.body.altHome)
    publishUpdate = true
  }

  if (publishUpdate) {
    updateWor()
  }

  if (req.file) {
    updateLogo(req.file)
  }

  theme
    .setCurrentThemeFromConfig()
    .then(() => {
      if (!req.accepts('text/html')) {
        return res.status(200).header('content-type', 'text/plain').send('Theme updated successfully')
      } else {
        form(req, res)
      }
    })
    .catch((err) => {
      res.status(500).send(err.stack)
    })
}
