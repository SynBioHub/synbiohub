
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var async = require('async')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var wiky = require('../wiky/wiky.js')

var config = require('../config')

var URI = require('sboljs').URI

var getUrisFromReq = require('../getUrisFromReq')

const tasks = require('../task/index')

const jobUtils = require('../jobs/job-utils')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    handlePost(req, res)
  } else {
    handleGet(req, res)
  }
}

function handleGet (req, res) {
  const { graphUri, uri, designId } = getUrisFromReq(req, res)

  fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
    console.log('res is ' + result)
    console.log('r2 is ' + result.sbol)
    console.log('graph uri is ' + result.graphUri)

    const locals = {
      config: config.get(),
      section: 'dataIntegration',
      user: req.user,
      graphUri: result.graphUri,
      inputUri: uri,
      tasks: tasks
    }

    res.send(pug.renderFile('templates/views/dataIntegration.jade', locals))
  })
}

function handlePost (req, res) {
  const graphUri = req.body.graphUri
  const inputUri = req.body.inputUri
  const tasks = JSON.parse(req.body.tasks)

  if (graphUri !== config.get('triplestore').defaultGraph &&
graphUri !== req.user.graphUri) {
    res.status(403).send('bad graph uri')
    return
  }

  jobUtils.createJob(req.user, graphUri, inputUri, tasks).then((job) => {
    res.redirect('/jobs')
  })
}
