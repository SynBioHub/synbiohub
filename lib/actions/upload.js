
const pug = require('pug')

const loadTemplate = require('../loadTemplate')

const getUrisFromReq = require('../getUrisFromReq')

const config = require('../config')

const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

const multiparty = require('multiparty')

const uploads = require('../uploads')

const attachments = require('../attachments')

const streamToString = require('stream-to-string')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

const SBOLDocument = require('sboljs')

var sparql = require('../sparql/sparql-collate')

const getOwnedBy = require('../query/ownedBy')

module.exports = function (req, res) {

	var attachmentObjects = []

	const form = new multiparty.Form()

	const { graphUri, uri, designId, share, url, baseUri } = getUrisFromReq(req, res)

	getOwnedBy(uri, graphUri).then((ownedBy) => {

		if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
			return res.status(401).send('not authorized to edit this submission')
		}

		form.on('part', (partStream) => {

			if (partStream.filename) {

				uploads.createUpload(partStream).then((uploadInfo) => {
				    console.log(JSON.stringify(uploadInfo))
					const { hash, size, mime } = uploadInfo
					console.log("Created upload!")
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

							attachmentList = results

							return attachments.getAttachmentsFromList(graphUri, attachmentList).then((results) => {

								attachmentObjects = results

							})
						})
					])

				}).then(() => {

					const locals = {
						config: config.get(),
						canEdit: true,
						url: url,
						attachments: attachmentObjects
					}

					res.send(pug.renderFile('templates/partials/attachments.jade', locals))

				}).catch((err) => {
				    console.log('ERR:'+err.stack)
				    res.status(500).send(err.stack)
				})

			}
		})

		form.on('error', (err) => {
			res.status(500).send(err)
		})

		form.parse(req)

	})

}


