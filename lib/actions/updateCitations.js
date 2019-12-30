
const pug = require('pug')

const sparql = require('../sparql/sparql')

const loadTemplate = require('../loadTemplate')

const config = require('../config')

const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

var retrieveCitations = require('../citations')

const getOwnedBy = require('../query/ownedBy')

module.exports = function (req, res) {
  const uri = req.body.uri

  const graphUri = getGraphUriFromTopLevelUri(uri, req.user)

  const citations = req.body.value

  var d = new Date()
  var modified = d.toISOString()
  modified = modified.substring(0, modified.indexOf('.'))

  const citationRegEx = /^[0-9]+(,[0-9]*)*$/
  if (citations && citations.trim() !== '' && !citationRegEx.test(citations)) {
    res.status(400).send('Citations must be comma separated Pubmed IDs')
    return
    // return new Promise(function (resolve, reject) {
    //   reject(new Error('Citations must be comma separated Pubmed IDs'))
    // })
  }

  var citationsSparql = ''
  if (citations.trim() !== '') {
    citationsSparql = '<' + uri + '> obo:OBI_0001617 ' + JSON.stringify(citations).replace(/,/g, '"; obo:OBI_0001617 "') + ' .'
  }

  const updateQuery = loadTemplate('./sparql/UpdateCitations.sparql', {
    topLevel: uri,
    citations: citationsSparql,
    modified: JSON.stringify(modified)
  })

  return getOwnedBy(uri, graphUri).then((ownedBy) => {
    if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
      res.status(401).send('not authorized to edit this submission')
      return
    }

    return sparql.updateQuery(updateQuery, graphUri).then((result) => {
      var templateParams = {
        uri: uri
      }

      var getCitationsQuery = loadTemplate('sparql/GetCitations.sparql', templateParams)
      var citationsQ
      var submissionCitations

      return sparql.queryJson(getCitationsQuery, graphUri).then((results) => {
        citationsQ = results
      }).then(() => {
        return retrieveCitations(citationsQ).then((resolvedCitations) => {
          submissionCitations = resolvedCitations

          // console.log('got citations ' + JSON.stringify(submissionCitations));
        }).then(() => {
          if (!req.accepts('text/html')) {
            return res.status(200).send('Success')
          } else {
            const locals = {
              config: config.get(),
              src: citations,
              submissionCitations: submissionCitations,
              canEdit: true
            }
            res.send(pug.renderFile('templates/partials/citations.jade', locals))
          }
        })
      })
    })
  })
}
