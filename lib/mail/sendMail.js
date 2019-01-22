
const helper = require('sendgrid').mail
const config = require('../config')

function sendMail (user, subject, message) {
  const fromEmail = new helper.Email(config.get('mail').fromAddress)
  const toEmail = new helper.Email(user.email)
  const content = new helper.Content('text/plain', message)
  const mail = new helper.Mail(fromEmail, subject, toEmail, content)

  const sg = require('sendgrid')(
    process.env.SENDGRID_API_KEY || config.get('mail').sendgridApiKey)

  var request = sg.emptyRequest({
    method: 'POST',
    path: '/v3/mail/send',
    body: mail.toJSON()
  })

  return new Promise((resolve, reject) => {
    sg.API(request, (err, response) => {
      if (err) {
        reject(err)
      } else {
        resolve(response)
      }
    })
  })
}

module.exports = sendMail
