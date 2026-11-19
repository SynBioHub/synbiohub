const axios = require('axios')
const { resolveMailCredentials } = require('./mailConfig')

function sendMail (user, subject, message) {
  const { apiKey, fromAddress } = resolveMailCredentials()

  return axios.post('https://api.resend.com/emails', {
    from: fromAddress,
    to: [user.email],
    subject: subject,
    text: message
  }, {
    headers: {
      Authorization: 'Bearer ' + apiKey,
      'Content-Type': 'application/json'
    }
  }).then((response) => {
    if (response.status < 200 || response.status >= 300) {
      throw new Error('Resend rejected email with status ' + response.status)
    }
    return response
  }).catch((err) => {
    if (err.response) {
      throw new Error('Resend rejected email with status ' + err.response.status)
    }
    throw err
  })
}

module.exports = sendMail
