const sendMail = require('./sendMail')
const loadTemplate = require('../loadTemplate')
const config = require('../config')

function sendCreatePasswordMail(user, administrator) {

    sendMail(user, 'Your SynBioHub Account', loadTemplate('mail/resetPassword.txt', {

        link: config.get('instanceUrl') + 'resetPassword/token/' + user.resetPasswordLink,
        administrator: administrator.name,
        username: user.username,

    }))

}

module.exports = sendCreatePasswordMail

