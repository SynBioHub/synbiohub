
const pug = require('pug')
const config = require('../../config')
const theme = require('../../theme')
const updateLogo = require('../../actions/admin/updateLogo')
const updateWor = require('../../actions/admin/updateWor')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    if (req.body.showModuleInteractions) {
      config.set('showModuleInteractions', true)
    } else {
      config.set('showModuleInteractions', false)
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

  const locals = {
    config: config.get(),
    instanceName: config.get('instanceName'),
    frontPageText: config.get('frontPageText'),
    section: 'admin',
    adminSection: 'theme',
    user: req.user,
    currentTheme: currentTheme,
    themeParameters: themeParameters
  }

  res.send(pug.renderFile('templates/views/admin/theme.jade', locals))
}

function post (req, res) {
  const currentTheme = theme.getCurrentTheme()

  const newParameters = {}

  currentTheme.parameters.map((parameter) => {
    const newParameter = req.body[parameter.variable]

    if (newParameter !== undefined) {
      if (newParameter !== parameter['default']) {
        newParameters[parameter.variable] = newParameter
      }
    }
  })

  const localConfig = config.getLocal()

  const themeName = theme.getCurrentThemeName()

  if (localConfig.themeParameters === undefined) { localConfig.themeParameters = {} }

  localConfig.themeParameters[themeName] = newParameters

  config.set(localConfig)

  let publishUpdate = false

  if (req.body.frontPageText != config.get('frontPageText')) {
    config.set('frontPageText', req.body.frontPageText)
    publishUpdate = true
  }

  if (req.body.instanceName != config.get('instanceName')) {
    config.set('instanceName', req.body.instanceName)
    publishUpdate = true
  }

  if (publishUpdate) {
    updateWor()
  }

  if (req.file) {
    updateLogo(req.file)
  }

  theme.setCurrentThemeFromConfig().then(() =>
    form(req, res)
  ).catch((err) => {
    res.status(500).send(err.stack)
  })
}
