var pug = require('pug')
// const request = require('request')
const config = require('../config')

module.exports = async function (req, res) {
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
  }

  if (req.body.maxAccepts.length > 0) {
    flags['maxaccepts'] = req.body.maxAccepts
  }
  if (req.body.id.length > 0) {
    flags['id'] = req.body.id
  }

  if (req.body.iddef !== 2) {
    flags['iddef'] = req.body.iddef
  }

  for (const [key, value] of Object.entries(flags)) {
    redirectString += `flag_${key}=${value}&`
  }
  res.redirect(redirectString)
}

// function searchText (sequence) {
//   return new Promise((resolve, reject) => {
//     request({
//       method: 'POST',
//       url: config.get('SBOLExplorerEndpoint') + 'sequencesearch',
//       body: { 'sequence': sequence },
//       json: true
//     }, function (error, response, body) {
//       if (error) {
//         reject(error)
//       }
//       resolve(body)
//     })
//   })
// }
