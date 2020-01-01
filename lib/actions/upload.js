
const pug = require('pug')

const loadTemplate = require('../loadTemplate')

const getUrisFromReq = require('../getUrisFromReq')

const config = require('../config')

const multiparty = require('multiparty')

const uploads = require('../uploads')

const attachments = require('../attachments')

var sparql = require('../sparql/sparql-collate')

const getOwnedBy = require('../query/ownedBy')

const edamOntology = require('edam-ontology')

module.exports = function (req, res) {
  var attachmentObjects = []

  const form = new multiparty.Form()

  const { graphUri, uri, url, baseUri } = getUrisFromReq(req, res)

  getOwnedBy(uri, graphUri).then((ownedBy) => {
    if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
      return res.status(401).send('not authorized to edit this submission')
    }

    form.on('part', (partStream) => {
      if (partStream.filename) {
        uploads.createUpload(partStream).then((uploadInfo) => {
          console.log(JSON.stringify(uploadInfo))
          const { hash, size, mime } = uploadInfo
          console.log('Created upload!')
          console.log(JSON.stringify(uploadInfo))

          return attachments.addAttachmentToTopLevel(
            graphUri, baseUri, uri, partStream.filename, hash, size,
            mime, req.user.username)
        }).then(() => {
          var templateParams = {
            uri: uri
          }

          var getAttachmentsQuery = loadTemplate('sparql/GetAttachments.sparql', templateParams)

          return Promise.all([
            sparql.queryJson(getAttachmentsQuery, graphUri).then((results) => {
              var attachmentList = results

              return attachments.getAttachmentsFromList(graphUri, attachmentList).then((results) => {
                attachmentObjects = results
              })
            })
          ])
        }).then(() => {
          var locals = {
            config: config.get(),
            canEdit: true,
            url: url,
            attachments: attachmentObjects
          }

          locals.attachmentTypes = Object.keys(edamOntology).map(key => {
            return {
              uri: key,
              name: edamOntology[key]
            }
          })
          locals.attachmentTypes.sort((a, b) => a.name.localeCompare(b.name))

          if (!req.accepts('text/html')) {
            res.status(200).type('text/plain').send('Success')
          } else {
            res.send(pug.renderFile('templates/partials/attachments.jade', locals))
          }
        }).catch((err) => {
          console.error(err.stack)
          res.status(400).send(err.stack)
        })
      }
    })

    form.on('error', (err) => {
      console.error(err.stack)
      res.status(400).send(err)
    })

    form.parse(req)
  })
}
