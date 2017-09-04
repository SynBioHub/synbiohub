
const loadTemplate = require('./loadTemplate')

const sliver = require('./sliver')

const config = require('./config')

const sparql = require('./sparql/sparql')

const filesize = require('filesize')

const assert = require('assert')

var URI = require('sboljs').URI

var sha1 = require('sha1');

const { fetchSBOLObjectRecursive } = require('./fetch/fetch-sbol-object-recursive')

function addAttachmentToTopLevel(graphUri, baseUri, topLevelUri, name, uploadHash, size, attachmentType, ownedBy) {

    const displayId = 'attachment_' + sliver.getId()
    const persistentIdentity = baseUri + '/' + displayId
    // TODO: should get version from topLevelUri
    const version = '1'
    const attachmentURI = persistentIdentity + '/' + version
    const collectionUri = baseUri + baseUri.slice(baseUri.lastIndexOf('/'))+'_collection/' + version

    const query = loadTemplate('./sparql/AttachUpload.sparql', {
	collectionUri: collectionUri,
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
	ownedBy: config.get('databasePrefix') + 'user/' + encodeURIComponent(ownedBy)
    })

    return sparql.updateQueryJson(query,graphUri).then((res) => {

        return Promise.resolve()


    })
}

function getTypeFromExtension(filename) {

    const extension = filename.slice(filename.lastIndexOf('.') + 1)
    
    return config.get('fileExtensionToAttachmentType')[extension]
                || 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#unknownAttachment'

}

function getAttachmentsFromTopLevel(sbol, topLevel, share) {

    const attachments = []

    topLevel.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachment').map((attachmentURI) => {

        const attachment = sbol.lookupURI(attachmentURI)

        if(attachment && !(attachment instanceof URI)) {

            const size = parseInt(attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentSize') || '-1')

	    var url = '/' + attachment.uri.toString().replace(config.get('databasePrefix'),'')
	    if (attachment.uri.toString().startsWith(config.get('databasePrefix')+'user/') && share) {
                url += '/' + sha1('synbiohub_' + sha1(attachment.uri.toString()) + config.get('shareLinkSalt')) + '/share'
	    }

            attachments.push({
                name: attachment.name,
                type: config.get('attachmentTypeToTypeName')[attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentType')] || 'Other',
                url: url,
                size: size,
                sizeString: size == -1 ? null : filesize(size)
            })

        }

    })

    return attachments
}


function getAttachmentsFromList(graphUri,attachmentList,share) {

    return Promise.all(
	attachmentList.map((attachmentURI) => {
 
	    var uri = attachmentURI.attachment

	    return fetchSBOLObjectRecursive(uri, graphUri).then((result) => {

		sbol = result.sbol
		attachment = result.object

		if(attachment) {

		    const size = parseInt(attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentSize') || '-1')

		    var url = '/' + attachment.uri.toString().replace(config.get('databasePrefix'),'')
		    if (attachment.uri.toString().startsWith(config.get('databasePrefix')+'user/') && share) {
			url += '/' + sha1('synbiohub_' + sha1(attachment.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		    }

		    return Promise.resolve({
			name: attachment.name,
			type: config.get('attachmentTypeToTypeName')[attachment.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#attachmentType')] || 'Other',
			url: url,
			size: size,
			sizeString: size == -1 ? null : filesize(size)
		    })
		}
	    }).catch((result) => {
		// TODO: skip, not found
		return Promise.resolve({
		    name: 'Attachment is missing',
		    type: 'Other',
		    url: '',
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
    getTypeFromExtension: getTypeFromExtension,
    getAttachmentsFromTopLevel: getAttachmentsFromTopLevel,
    getAttachmentsFromList: getAttachmentsFromList
}

