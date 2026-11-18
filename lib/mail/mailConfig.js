const config = require('../config')

function getMailConfig () {
  return config.get('mail') || {}
}

function resolveMailApiKey () {
  const mail = getMailConfig()
  const envKey = process.env.RESEND_API_KEY
  if (envKey && envKey.trim() !== '') {
    return envKey.trim()
  }

  const resendKey = (mail.resendApiKey || '').trim()
  if (resendKey !== '') {
    return resendKey
  }

  return (mail.sendgridApiKey || '').trim()
}

function resolveFromAddress () {
  const mail = getMailConfig()
  return (mail.fromAddress || '').trim()
}

function isMailConfigured () {
  return resolveMailApiKey() !== ''
}

function resolveMailCredentials () {
  const apiKey = resolveMailApiKey()
  const fromAddress = resolveFromAddress()

  if (apiKey === '' || fromAddress === '') {
    throw new Error('Mail settings are not configured.')
  }

  return { apiKey, fromAddress }
}

module.exports = {
  getMailConfig,
  resolveMailApiKey,
  resolveFromAddress,
  isMailConfigured,
  resolveMailCredentials
}
