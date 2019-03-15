const loadTemplate = require('./loadTemplate')
const config = require('./config')
const uriToMeta = require('./uriToMeta')
const sparql = require('./sparql/sparql')
const filesize = require('filesize')
const uuid = require('uuid/v4')
const URI = require('sboljs').URI
const sha1 = require('sha1')
const uuid = require('uuid/v4')
const { fetchSBOLObjectRecursive } = require('./fetch/fetch-sbol-object-recursive')

function addAttachmentToTopLevel (graphUri, baseUri, topLevelUri, name, uploadHash, size, attachmentType, owner) {
// console.log('Adding:'+name+' to:'+topLevelUri)
  const displayId = 'attachment_' + uuid().replace(/-/g, '')
  const persistentIdentity = baseUri + '/' + displayId
  // TODO: should get version from topLevelUri
  const version = '1'
  const attachmentURI = persistentIdentity + '/' + version
  const collectionUri = baseUri + baseUri.slice(baseUri.lastIndexOf('/')) + '_collection/' + version
  const ownedBy = config.get('databasePrefix') + 'user/' + encodeURIComponent(owner)

  const query = loadTemplate('./sparql/AttachUpload.sparql', {
    collectionUri: collectionUri,
    topLevel: topLevelUri,
    attachmentURI: attachmentURI,
    attachmentSource: attachmentURI + '/download',
    persistentIdentity: persistentIdentity,
    displayId: JSON.stringify(displayId),
    version: JSON.stringify(version),
    name: JSON.stringify(name),
    description: JSON.stringify(''),
    hash: JSON.stringify(uploadHash),
    size: JSON.stringify(size + ''),
    type: attachmentType,
    ownedBy: ownedBy
  })

  return sparql.updateQueryJson(query, graphUri).then((res) => {
    // console.log('Added:'+name+' to:'+topLevelUri)
    // console.log("Res: " + res)
    // console.log("AttachmentURI: " + attachmentURI)
    return attachmentURI
  })
}

function updateAttachment (graphUri, attachmentURI, uploadHash, size) {
// console.log('Adding:'+name+' to:'+topLevelUri)
// TODO: should get version from topLevelUri

  const query = loadTemplate('./sparql/UpdateAttachment.sparql', {
    attachmentURI: attachmentURI,
    attachmentSource: attachmentURI + '/download',
    hash: JSON.stringify(uploadHash),
    size: JSON.stringify(size + '')
  })

  return sparql.updateQueryJson(query, graphUri).then((res) => {
    return attachmentURI
  })
}

function getTypeFromExtension (filename) {
  const extension = filename.slice(filename.lastIndexOf('.') + 1)

  return config.get('fileExtensionToAttachmentType')[extension] ||
'http://wiki.synbiohub.org/wiki/Terms/synbiohub#unknownAttachment'
}

function getAttachmentsFromTopLevel (sbol, topLevel, share) {
  const attachments = []

  topLevel.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachment').map((attachmentURI) => {
    const attachment = sbol.lookupURI(attachmentURI)

    if (attachment && !(attachment instanceof URI)) {
      const size = parseInt(attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentSize') || '-1')

      var url = '/' + attachment.uri.toString().replace(config.get('databasePrefix'), '')
      if (attachment.uri.toString().startsWith(config.get('databasePrefix') + 'user/') && share) {
        url += '/' + sha1('synbiohub_' + sha1(attachment.uri.toString()) + config.get('shareLinkSalt')) + '/share'
      }

      attachments.push({
        name: attachment.name,
        type: config.get('attachmentTypeToTypeName')[attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentType')] || 'Other',
        url: url,
        size: size,
        sizeString: size === -1 ? null : filesize(size)
      })
    }
  })

  topLevel.attachments.map(attachment => {
    if (attachment) {
      if (!(attachment instanceof URI)) {
        var url = '/' + attachment.uri.toString().replace(config.get('databasePrefix'), '')

        if (attachment.uri.toString().startsWith(config.get('databasePrefix') + 'user/') && share) {
          url += '/' + sha1('synbiohub_' + sha1(attachment.uri.toString()) + config.get('shareLinkSalt')) + '/share'
        }

        var attachmentType = attachment.format.toString()
          .replace('http://identifiers.org/combine.specifications/', '')
          .replace('http://identifiers.org/edam/', '')
          .replace('http://purl.org/NET/mediatypes/application/', '')
          .replace('http://purl.org/NET/mediatypes/audio/', '')
          .replace('http://purl.org/NET/mediatypes/font/', '')
          .replace('http://purl.org/NET/mediatypes/example/', '')
          .replace('http://purl.org/NET/mediatypes/image/', '')
          .replace('http://purl.org/NET/mediatypes/message/', '')
          .replace('http://purl.org/NET/mediatypes/model/', '')
          .replace('http://purl.org/NET/mediatypes/multipart/', '')
          .replace('http://purl.org/NET/mediatypes/text/', '')
          .replace('http://purl.org/NET/mediatypes/video/', '')

        var attachmentIsImage = attachment.format.toString().indexOf('png') >= 0 ||
            attachment.format.toString() === 'http://identifiers.org/edam/format_3603' ||
            attachment.format.toString().indexOf('jpeg') >= 0 ||
            attachment.format.toString() === 'http://identifiers.org/edam/format_3579' ||
            attachment.format.toString().indexOf('bmp') >= 0 ||
            attachment.format.toString() === 'http://identifiers.org/edam/format_3592' ||
            attachment.format.toString().indexOf('tiff') >= 0 ||
            attachment.format.toString() === 'http://identifiers.org/edam/format_3591' ||
            attachment.format.toString().indexOf('gif') >= 0 ||
            attachment.format.toString() === 'http://identifiers.org/edam/format_3467' ||
            attachment.format.toString().indexOf('imageAttachment') >= 0 ||
            attachment.format.toString().indexOf('image') >= 0

        attachments.push({
          name: attachment.name || attachment.displayId,
          type: attachmentType,
          image: attachmentIsImage,
          url: url,
          size: attachment.size,
          sizeString: attachment.size === -1 ? null : filesize(attachment.size)
        })
      } else {
        url = '/' + attachment.toString().replace(config.get('databasePrefix'), '')
        if (attachment.toString().startsWith(config.get('databasePrefix') + 'user/') && share) {
          url += '/' + sha1('synbiohub_' + sha1(attachment.toString()) + config.get('shareLinkSalt')) + '/share'
        }

        let metaData = uriToMeta(attachment)
        attachments.push({
          name: metaData.name,
          type: 'unknown',
          url: url
        })
      }
    }
  })

  return attachments
}

function getAttachmentsFromList (graphUri, attachmentList, share) {
  return Promise.all(
    attachmentList.map((attachmentURI) => {
      var uri = attachmentURI.attachment

      return fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
        let attachment = result.object

        if (attachment) {
          let format = attachment.format || attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentType')
          let size = attachment.size || attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentSize')

          format = format.toString()
          size = size.toString()

          size = parseInt(size)

          format = format.toString()
            .replace('http://identifiers.org/combine.specifications/', '')
            .replace('http://identifiers.org/edam/', '')
            .replace('http://purl.org/NET/mediatypes/application/', '')
            .replace('http://purl.org/NET/mediatypes/audio/', '')
            .replace('http://purl.org/NET/mediatypes/font/', '')
            .replace('http://purl.org/NET/mediatypes/example/', '')
            .replace('http://purl.org/NET/mediatypes/image/', '')
            .replace('http://purl.org/NET/mediatypes/message/', '')
            .replace('http://purl.org/NET/mediatypes/model/', '')
            .replace('http://purl.org/NET/mediatypes/multipart/', '')
            .replace('http://purl.org/NET/mediatypes/text/', '')
            .replace('http://purl.org/NET/mediatypes/video/', '')

          var url = '/' + attachment.uri.toString().replace(config.get('databasePrefix'), '')

          if (attachment.uri.toString().startsWith(config.get('databasePrefix') + 'user/') && share) {
            url += '/' + sha1('synbiohub_' + sha1(attachment.uri.toString()) + config.get('shareLinkSalt')) + '/share'
          }

          return Promise.resolve({
            name: attachment.name,
            type: format || 'Other',
            url: url,
            size: size,
            sizeString: size === -1 ? null : filesize(size)
          })
        }
      }).catch((result) => {
        // TODO: skip, not found
        return Promise.resolve({
          name: 'Attachment is missing',
          type: 'Other',
          url: uri,
          size: 0,
          sizeString: null
        })
      })
    })
  ).then((attachments) => {
    return Promise.resolve(attachments)
  })
}

module.exports = {
  addAttachmentToTopLevel: addAttachmentToTopLevel,
  updateAttachment: updateAttachment,
  getTypeFromExtension: getTypeFromExtension,
  getAttachmentsFromTopLevel: getAttachmentsFromTopLevel,
  getAttachmentsFromList: getAttachmentsFromList
}
