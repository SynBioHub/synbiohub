const sendMail = require('./sendMail')
const loadTemplate = require('../loadTemplate')
const config = require('../config')

function sendCreatePasswordMail (user, administrator) {
  return sendMail(user, 'Your SynBioHub Account', loadTemplate('mail/createPassword.txt', {
    link: config.get('instanceUrl') + 'resetPassword/token/' + user.resetPasswordLink,
    administrator: administrator.name,
    name: user.name || '',
    username: user.username || '',
    email: user.email || '',
    affiliation: user.affiliation || ''
  }))
}

module.exports = sendCreatePasswordMail
