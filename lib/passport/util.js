const { exec } = require('child_process')

const dependencies = {
  google: 'passport-google-oauth@~2.0.0'
}

const installDependencies = (providerName) => {
  if (!providerName) return

  if (!dependencies[providerName]) {
    throw new Error(`Auth dependency for [${providerName}] is not defined`)
  }

  return new Promise((resolve, reject) => {
    const packageName = dependencies[providerName]

    console.log(`Installing package: ${packageName}`)

    exec(`yarn add ${packageName}`, (err) => {
      if (err) {
        reject(err)
      }

      resolve()
    })
  })
}

module.exports = { installDependencies }
