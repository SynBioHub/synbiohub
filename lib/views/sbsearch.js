const pug = require('pug')
const config = require('../config')
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
  page.keepExtensions = true
  page.parse(req, function (err, fields, files) {
    if (err) {
      console.log(err)
    }

    var body = fields

    searchSequence = body.sequenceInput.replace(/[\n\r\\n\\r]/g, '')

    if (files.sequenceFile.size > 0 && files.sequenceFile.name.trim().length > 0) {
      if (body.searchMethod === 'exact') { flags['search_exact'] = true }
      redirectString += `file_search=${encodeURIComponent(files.sequenceFile.path)}&`
    } else if (body.searchMethod === 'global') {
      redirectString += `globalsequence=${searchSequence}&`
    } else if (body.searchMethod === 'exact') {
      redirectString += `sequence=${searchSequence}&`
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
      redirectString += `${key}=${value}&`
    }
    res.redirect(redirectString)
  })
}
