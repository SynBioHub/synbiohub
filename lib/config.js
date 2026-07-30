
var fs = require('fs')
var extend = require('xtend')
var deepmerge = require('deepmerge')

var path = 'config.json'
var localPath = 'config.local.json'

var config = JSON.parse(fs.readFileSync(path) + '')

var localConfig = fs.existsSync(localPath)
  ? JSON.parse(fs.readFileSync(localPath) + '') : {}

var mergedConfig

mergeConfigs()

module.exports = {

  get: function configGet (key) {
    if (arguments.length === 0) { return mergedConfig }

    return mergedConfig[key]
  },

  getLocal: function configGetLocal () {
    return clone(localConfig)
  },

  set: function configSet (key, value) {
    if (arguments.length === 1) {
      localConfig = clone(arguments[0])

      mergeConfigs()
      saveLocalConfig()

      return
    }

    console.log('config: set ' + key + ' => ' + JSON.stringify(value))

    localConfig = extend(localConfig, {

      [key]: clone(value)

    })

    mergeConfigs()
    saveLocalConfig()
  },

  delete: function configDelete (key) {
    if (arguments.length === 1) {
      if (localConfig[key] !== undefined) { delete localConfig[key] }

      mergeConfigs()
      saveLocalConfig()
    }
  },

  merge: function configMerge (newConfig) {
    localConfig = deepmerge(localConfig, newConfig, {
      arrayMerge: (dest, src) => src
    })

    mergeConfigs()
    saveLocalConfig()
  }

}

function clone (val) {
  return JSON.parse(JSON.stringify(val))
}

function mergeConfigs () {
  mergedConfig = deepmerge(config, localConfig, {
    arrayMerge: (dest, src) => src
  })
  applyEnvironmentOverrides()
}

// Deployment-only overrides for the external search topology. These values
// deliberately sit above config.local.json without being persisted back into
// it, so the same SynBioHub image can be exercised against stock SBOLExplorer
// and sbol-db's compatibility listener in isolated Compose projects.
function applyEnvironmentOverrides () {
  if (process.env.SBH_EXPLORER_ENDPOINT !== undefined) {
    if (process.env.SBH_EXPLORER_ENDPOINT === '') {
      throw new Error('SBH_EXPLORER_ENDPOINT cannot be empty')
    }
    mergedConfig.SBOLExplorerEndpoint = process.env.SBH_EXPLORER_ENDPOINT
  }
  if (process.env.SBH_USE_EXPLORER !== undefined) {
    mergedConfig.useSBOLExplorer = parseEnvironmentBoolean(
      'SBH_USE_EXPLORER', process.env.SBH_USE_EXPLORER)
  }
  if (process.env.SBH_EXPLORER_FALLBACK !== undefined) {
    mergedConfig.SBOLExplorerFallback = parseEnvironmentBoolean(
      'SBH_EXPLORER_FALLBACK', process.env.SBH_EXPLORER_FALLBACK)
  }
}

function parseEnvironmentBoolean (name, value) {
  switch (String(value).trim().toLowerCase()) {
    case '1':
    case 'true':
    case 'yes':
    case 'on':
      return true
    case '0':
    case 'false':
    case 'no':
    case 'off':
      return false
    default:
      throw new Error(name + ' must be a boolean (true/false, 1/0, yes/no, on/off)')
  }
}

function saveLocalConfig () {
  fs.writeFileSync(localPath, JSON.stringify(localConfig, null, 2))
}
