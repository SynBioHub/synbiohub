var pug = require('pug')
// const request = require('request')
const config = require('../config')
const fs = require('fs')

module.exports = async function (req, res) {
  res.setHeader('Cache-Control', 'no-cache')
  res.setHeader('Cache-Control', 'no-store')
  res.setHeader('Pragma', 'no-cache')

  if (req.method === 'POST') {
    post(req, res)
  } else {
    form(req, res)
  }
}

var searchSequence
var redirectString = '/search/'

function form (req, res) {
  var locals = {
    config: config.get(),
    section: 'admin',
    adminSection: 'explorer',
    user: req.user
  }
  redirectString = '/search/'
  res.send(pug.renderFile('templates/views/sbsearch.jade', locals))
}

function post (req, res) {
  var flags = {}

  searchSequence = req.body.sequenceInput

  if (req.body.searchMethod === 'global') {
    redirectString += `globalsequence=${searchSequence}&`
  } else {
    redirectString += `sequence=${searchSequence}&`
    flags['searchexact'] = true
  }

  if (req.body.maxAccepts.length > 0) {
    flags['maxaccepts'] = req.body.maxAccepts
  }

  if (req.body.maxRejects.length > 0) {
    flags['maxrejects'] = req.body.maxRejects
  }

  if (req.body.id.length > 0) {
    flags['id'] = req.body.id
  }

  if (req.body.iddef !== '2') {
    flags['iddef'] = req.body.iddef
  }

  if (req.body.maxSeqLength > 0) {
    flags['maxseqlength'] = req.body.maxSeqLength
  }

  if (req.body.minSeqLength > 0) {
    flags['minseqlength'] = req.body.minSeqLength
  }

  for (const [key, value] of Object.entries(flags)) {
    redirectString += `flag_${key}=${value}&`
  }

  if (req.files !== undefined) {
    var sequenceb64 = fs.readFileSync(req.files.sequenceFile)
    searchFile(sequenceb64, flags)
  }

  res.redirect(redirectString)
}

function searchFile (sequence, flags) {
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
