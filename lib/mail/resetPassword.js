
const sendMail = require('./sendMail')
const loadTemplate = require('../loadTemplate')
const config = require('../config')

function sendResetPasswordMail (user) {
  sendMail(user, 'Reset your password', loadTemplate('mail/resetPassword.txt', {

    link: config.get('instanceUrl') + 'resetPassword/token/' + user.resetPasswordLink

  }))
}

module.exports = sendResetPasswordMail
