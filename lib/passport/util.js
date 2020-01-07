const { exec } = require('child_process')
const { authDependencies } = require('../../package.json')

const installDependencies = (providerName) => {
  if (!providerName) return

  if (!authDependencies[providerName]) {
    throw new Error(`Auth dependency for [${providerName}] is not defined`)
  }

  return new Promise((resolve, reject) => {
    const packageName = authDependencies[providerName]

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
