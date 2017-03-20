
const request = require('request')
const config = require('../../../config')

const ice = require('../../../ice')

const splitUri = require('../../../splitUri')

function getCollectionMemberCount(remoteConfig, uri) {

    const { displayId } = splitUri(uri)

    if(displayId === remoteConfig.rootCollection.displayId) {

        return ice.getRootFolderCount(remoteConfig)
    }

    if(displayId.indexOf(remoteConfig.folderPrefix) !== 0) {

        res.status(404).send('???')
        return

    }

    const folderId = parseInt(displayId.slice(remoteConfig.folderPrefix.length))

    return ice.getFolderEntryCount(remoteConfig, folderId)
}

function getRootCollectionMetadata(remoteConfig) {

    return Promise.resolve([
        {
            uri: 'public/' + remoteConfig.id +
                        '/' + remoteConfig.rootCollection.displayId
                            + '/current',
            version: 'current',
            name: remoteConfig.rootCollection.name,
            description: remoteConfig.rootCollection.description,
            wasDerivedFrom: remoteConfig.url,
            remote: true
        }
    ])

}

function getContainingCollections(remoteConfig, uri) {

    // TODO
    //
    return Promise.resolve([])

}


function getCollectionMembers(remoteConfig, uri, limit, offset) {

    const { displayId } = splitUri(uri)

    if(displayId === remoteConfig.rootCollection.displayId) {

        return ice.getRootFolders(remoteConfig).then(foldersToCollections)
    }

    const folderId = parseInt(displayId.slice(remoteConfig.folderPrefix.length))

    return ice.getFolderEntries(remoteConfig, folderId).then(entriesToComponentDefinitions)

    function foldersToCollections(folders) {

        return Promise.resolve(folders.map((folder) => {
            return {
                type: 'http://sbols.org/v2#Collection',
                displayId: remoteConfig.folderPrefix + folder.id,
                version: 'current',
                uri: '/public/' + remoteConfig.id + '/' + remoteConfig.folderPrefix + folder.id + '/current',
                name: folder.folderName,
                description: folder.description,
                remote: true
            }
        }))

    }

    function entriesToComponentDefinitions(entries) {

        console.log(JSON.stringify(entries))

        const version = 'current'

        return Promise.resolve(entries.map((entry) => {
            
            return {
                type: 'http://sbols.org/v2#ComponentDefinition',
                uri: '/public/' + remoteConfig.id + '/' + entry.partId + '/' + version,
                displayId: entry.partId,
                version: version,
                name: entry.name,
                description: entry.shortDescription,
                remote: true
            }
        }))

    }

}

function getCollectionMetaData(remoteConfig, uri) {

    const { displayId } = splitUri(uri)

    if(displayId === remoteConfig.rootCollection.displayId) {

        return getRootCollectionMetadata(remoteConfig).then(
                    (metadata) => Promise.resolve(metadata[0]))


    }

    const folderId = parseInt(displayId.slice(remoteConfig.folderPrefix.length))

    return ice.getFolder(remoteConfig, folderId).then((folder) => {

        return Promise.resolve({
            type: 'http://sbols.org/v2#Collection',
            displayId: remoteConfig.folderPrefix + folder.id,
            version: 'current',
            uri: '/public/' + remoteConfig.id + '/' + remoteConfig.folderPrefix + folder.id + '/current',
            name: folder.folderName,
            description: folder.description,
            remote: true
        })

    })

}

module.exports = {
    getCollectionMemberCount: getCollectionMemberCount,
    getRootCollectionMetadata: getRootCollectionMetadata,
    getContainingCollections: getContainingCollections,
    getCollectionMembers: getCollectionMembers,
    getCollectionMetaData: getCollectionMetaData
}


