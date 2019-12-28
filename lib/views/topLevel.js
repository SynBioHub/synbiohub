var { getType } = require('../query/type')
var config = require('../config')
var topLevelView = require('./topLevelView')
var pug = require('pug')
var getUrisFromReq = require('../getUrisFromReq')
var sbol = require('../api/sbol')

module.exports = function (req, res) {
  const { graphUri, uri } = getUrisFromReq(req, res)

  getType(uri, graphUri).then((result) => {
    if (!req.accepts('text/html')) {
      sbol(req, res)
    } else {
      topLevelView(req, res, result)
    }
  }).catch((err) => {
    if (!req.accepts('text/html')) {
      return res.status(404).send(uri + ' not found')
    } else {
      var locals = {
        config: config.get(),
        section: 'errors',
        user: req.user,
        errors: [ err.stack ]
      }
      res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    }
  })
}
