const pug = require('pug')
const config = require('../../config')
const theme = require('../../theme')
const updateLogo = require('../../actions/admin/updateLogo')
const updateWor = require('../../actions/admin/updateWor')
const extend = require('xtend')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    if (req.body.showModuleInteractions) {
      config.set('showModuleInteractions', true)
    } else {
      config.set('showModuleInteractions', false)
    }
    if (req.body.removePublicEnabled) {
      config.set('removePublicEnabled', true)
    } else {
      config.set('removePublicEnabled', false)
    }
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
    currentTheme: currentTheme,
    themeParameters: themeParameters
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
