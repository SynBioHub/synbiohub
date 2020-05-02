const config = require('../../config')
const request = require('request')

module.exports = function (req, res) {
  let data = {
    instanceUrl: config.get('instanceUrl'),
    uriPrefix: config.get('databasePrefix'),
    administratorEmail: req.body.administratorEmail,
    updateEndpoint: 'updateWebOfRegistries',
    name: config.get('instanceName'),
    description: config.get('frontPageText')
  }

  if (!req.body.administratorEmail) {
    if (!req.accepts('text/html')) {
      return res.status(400).header('content-type', 'text/plain').send('Must provide a valid administrator email address')
    } else {
      res.redirect('/admin/registries')
    }
  }

  if (!req.body.webOfRegistries) {
    if (!req.accepts('text/html')) {
      return res.status(400).header('content-type', 'text/plain').send('Must provide a valid Web-of-Registries URL')
    } else {
      res.redirect('/admin/registries')
    }
  }

  config.set('administratorEmail', req.body.administratorEmail)

  let worUrl = req.body.webOfRegistries[-1] !== '/' ? req.body.webOfRegistries : req.body.webOfRegistries.substring(0, -1)

  request.post(worUrl + '/instances/new/', {
    json: data
  }, (err, resp, body) => {
    if (err) {
      console.error('Federation error')
      console.error(err)
      if (!req.accepts('text/html')) {
        return res.status(503).header('content-type', 'text/plain').send('Problem contacting Web-of-Registries')
      } else {
        // TODO: Display some kind of error message
        res.redirect('/admin/registries')
      }
    } else if (resp.statusCode >= 300) {
      console.error('Error (' + resp.statusCode + ') while communicating with Web-of-Registries (' + worUrl + ')')
      if (!req.accepts('text/html')) {
        return res.status(resp.statusCode).header('content-type', 'text/plain').send('Problem contacting Web-of-Registries')
      } else {
        // TODO: Display some kind of error message
        res.redirect('/admin/registries')
      }
    } else {
      config.set('webOfRegistriesSecret', body['updateSecret'])
      config.set('webOfRegistriesUrl', worUrl)
      config.set('webOfRegistriesId', body['id'])
      if (!req.accepts('text/html')) {
        return res.status(200).header('content-type', 'text/plain').send('Submitted request to join Web-of-Registries')
      } else {
        res.redirect('/admin/registries')
      }
    }
  })
}
