var pug = require('pug')
const request = require('request')
const config = require('../config')

module.exports = async function (req, res) {
  if (req.method === 'POST') {
    post(req, res)
  } else {
    form(req, res)
  }
}

var searchSequence
var redirectString = '/search/sequence=advancedsequencesearch&'

function form (req, res) {
  var locals = {
    config: config.get(),
    section: 'admin',
    adminSection: 'explorer',
    user: req.user
  }
  res.send(pug.renderFile('templates/views/sbsearch.jade', locals))
}

function post (req, res) {
  var flags = {}

  searchSequence = req.body.sequenceInput
  if (req.body.searchMethod === 'global') {
    flags['exactSearch'] = false
  } else if (req.body.searchMethod === 'exact') {
    flags['exactSearch'] = true
  }

  if (req.body.maxAccepts.length > 0) {
    flags['maxAccepts'] = req.body.maxAccepts
  }
  if (req.body.id.length > 0) {
    flags['id'] = req.body.id
  }
  searchText(searchSequence, flags).then(() => {
    console.log('Sequence search complete.')
    res.redirect(redirectString)
  })
}

function searchText (sequence, flags) {
  return new Promise((resolve, reject) => {
    request({
      method: 'POST',
      url: config.get('SBOLExplorerEndpoint') + 'sequencesearch',
      body: { 'sequence': sequence, 'flags': flags },
      json: true
    }, function (error, response, body) {
      if (error) {
        reject(error)
      }
      resolve(body)
    })
  })
}
