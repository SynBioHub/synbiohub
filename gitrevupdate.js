#!/usr/bin/env node

const config = require('./lib/config')
const gitRev = require('./lib/gitRevision')

console.log('Updating git revision in configuration...')

// Get the current git revision
const revision = gitRev()

if (revision) {
  // Set the revision in the config
  config.set('revision', revision)
  console.log(`Git revision updated to: ${revision}`)
} else {
  console.error('Failed to get git revision')
  process.exit(1)
}