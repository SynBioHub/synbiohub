
const loadTemplate = require('./loadTemplate')

const sliver = require('./sliver')

const config = require('./config')

const sparql = require('./sparql/sparql')

const filesize = require('filesize')

function addAttachmentToTopLevel(graphUri, baseUri, topLevelUri, name, uploadHash, size, attachmentType, ownedBy) {

    const displayId = 'attachment_' + sliver.getId()
    const persistentIdentity = baseUri + '/' + displayId
    const version = '1'
    const attachmentURI = persistentIdentity + '/' + version

    const query = loadTemplate('./sparql/AttachUpload.sparql', {
        topLevel: topLevelUri,
        attachmentURI: attachmentURI,
        persistentIdentity: persistentIdentity,
        displayId: JSON.stringify(displayId),
        version: JSON.stringify(version),
        name: JSON.stringify(name),
        description: JSON.stringify(""),
        hash: JSON.stringify(uploadHash),
        size: JSON.stringify(size + ''),
        type: attachmentType,
	ownedBy: JSON.stringify(config.get('databasePrefix') + 'user/' + encodeURIComponent(ownedBy))
    })

    return sparql.queryJson(query,graphUri).then((res) => {

        return Promise.resolve()


    })
}

function getTypeFromExtension(filename) {

    const extension = filename.slice(filename.lastIndexOf('.') + 1)
    
    return config.get('fileExtensionToAttachmentType')[extension]
                || 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#unknownAttachment'

}

function getAttachmentsFromTopLevel(sbol, topLevel) {

    const attachments = []

    topLevel.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachment').map((attachmentURI) => {

        const attachment = sbol.lookupURI(attachmentURI)

        const size = parseInt(attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentSize') || '-1')

        if(attachment) {

            attachments.push({
                name: attachment.name,
                type: config.get('attachmentTypeToTypeName')[attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentType')] || 'Other',
                url: '/' + attachment.uri.toString().replace(config.get('databasePrefix'),''),
                size: size,
                sizeString: size == -1 ? null : filesize(size)
            })

        }

    })

    return attachments
}

module.exports = {
    addAttachmentToTopLevel: addAttachmentToTopLevel,
    getTypeFromExtension: getTypeFromExtension,
    getAttachmentsFromTopLevel: getAttachmentsFromTopLevel
}

