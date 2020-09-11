const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const pug = require('pug')
const getDisplayList = require('visbol').getDisplayList.getDisplayList
const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')

module.exports = function (req, res) {
  var locals = {
    config: config.get(),
    section: 'component',
    user: req.user
  }

  const {
    graphUri,
    uri
  } = getUrisFromReq(req, res)

  fetchSBOLObjectRecursive('ComponentDefinition', uri, graphUri).then((result) => {
    var componentDefinition = result.object

    return componentDefinition
  }).then(componentDefinition => {
    locals.meta = {
      displayList: getDisplayList(componentDefinition, config, req.url.toString().endsWith('/share'))
    }

    res.send(pug.renderFile('templates/views/visualization.jade', locals))
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
