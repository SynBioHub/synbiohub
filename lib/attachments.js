
const loadTemplate = require('./loadTemplate')

const sliver = require('./sliver')

const config = require('./config')

const sparql = require('./sparql/sparql')

function addAttachmentToTopLevel(graphUri, baseUri, topLevelUri, name, uploadHash, attachmentType) {

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
        type: attachmentType
    })

    console.log(query)

    return sparql.queryJson(query).then((res) => {

        return Promise.resolve()


    })
}

function getTypeFromExtension(filename) {

    const extension = filename.slice(filename.lastIndexOf('.') + 1)
    
    return config.get('fileExtensionToAttachmentType')[extension]
                || 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#unknownAttachment'

}

module.exports = {
    addAttachmentToTopLevel: addAttachmentToTopLevel,
    getTypeFromExtension: getTypeFromExtension
}

