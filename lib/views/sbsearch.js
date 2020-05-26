const pug = require('pug')
const config = require('../config')
const fs = require('fs')
const request = require('request')
const formidable = require('formidable')

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

  var page = new formidable.IncomingForm()
  page.parse(req, function (err, fields, files) {
    if (err) {
      console.log(err)
    }

    var body = fields

    searchSequence = body.sequenceInput

    if (body.searchMethod === 'global') {
      redirectString += `globalsequence=${searchSequence}&`
    } else {
      redirectString += `sequence=${searchSequence}&`
      flags['searchexact'] = true
    }

    if (body.maxAccepts.length > 0) {
      flags['maxaccepts'] = body.maxAccepts
    }

    if (body.maxRejects.length > 0) {
      flags['maxrejects'] = body.maxRejects
    }

    if (body.id.length > 0) {
      flags['id'] = body.id
    }

    if (body.iddef !== '2') {
      flags['iddef'] = body.iddef
    }

    if (body.maxSeqLength > 0) {
      flags['maxseqlength'] = body.maxSeqLength
    }

    if (body.minSeqLength > 0) {
      flags['minseqlength'] = body.minSeqLength
    }

    for (const [key, value] of Object.entries(flags)) {
      redirectString += `flag_${key}=${value}&`
    }
    if (files.sequenceFile.size > 0 && files.sequenceFile.name.trim().length > 0) {
      var sequenceb64 = fs.readFileSync(files.sequenceFile.path, { encoding: 'base64' })
      var fileName = files.sequenceFile.name.split('.')
      var fileExtension = fileName[fileName.length - 1]
      var locals = fileSearch(sequenceb64, flags, fileExtension)
      res.send(pug.renderFile('templates/views/search.jade', locals))
    } else {
      res.redirect(redirectString)
    }
  })
}

function fileSearch (encodedFile, flags, fileType = 'N/A') {
  return new Promise((resolve, reject) => {
    request({
      method: 'POST',
      url: config.get('SBOLExplorerEndpoint') + 'sequencesearch',
      body: { 'sequence': encodedFile, 'flags': flags, 'fileType': fileType },
      json: true
    }, function (error, response, body) {
      if (error) {
        reject(error)
      }
      resolve(body)
    })
  })
}
