const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const pug = require('pug')
const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')
const summarizeSBOL = require('../summarize/summarizeSBOL')
const { getType } = require('../query/type')
const { getShortName } = require('./topLevelView')

module.exports = async function (req, res) {
  const locals = {
    config: config.get(),
    section: 'component',
    user: req.user
  }

  const { graphUri, uri } = getUrisFromReq(req, res)

  try {
    const type = await getType(uri, graphUri)

    const result = await fetchSBOLObjectRecursive(uri, graphUri)
    const sbol = result.sbol
    const topLevel = result.object

    let typeShortName = getShortName(type)

    locals.meta = summarizeSBOL(
      typeShortName, topLevel, req, sbol, null, graphUri
    )

    return res.send(
      pug.renderFile('templates/views/visualization.jade', locals)
    )
  } catch (err) {
    if (!req.accepts('text/html')) {
      return res.status(404).send(uri + ' not found')
    }

    const errorLocals = {
      config: config.get(),
      section: 'errors',
      user: req.user,
      errors: [err.stack]
    }

    return res.send(
      pug.renderFile('templates/views/errors/errors.jade', errorLocals)
    )
  }
}
