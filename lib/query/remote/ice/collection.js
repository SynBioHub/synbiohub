
const request = require('request')
const config = require('../../../config')

const ice = require('../../../ice')

const splitUri = require('../../../splitUri')

function getCollectionMemberCount(remoteConfig, uri) {

    const { displayId } = splitUri(uri)

    if(displayId === remoteConfig.rootCollection.displayId) {

        return ice.getRootFolderCount(remoteConfig)
    }

    if(displayId.indexOf('ice_folder_') !== 0) {

        res.status(404).send('???')
        return

    }

    const folderId = parseInt(displayId.split('ice_folder_')[1])

    return ice.getFolderEntryCount(remoteConfig, folderId)
}

function getRootCollectionMetadata(remoteConfig) {

    return Promise.resolve([
        {
            uri: '/public/' + remoteConfig.id +
                        '/' + remoteConfig.rootCollection.displayId
                            + '/' + remoteConfig.rootCollection.version,
            version: remoteConfig.rootCollection.version,
            name: remoteConfig.rootCollection.name,
            description: remoteConfig.rootCollection.description
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

    const folderId = parseInt(displayId.split('ice_folder_')[1])

    return ice.getFolderEntries(remoteConfig, folderId).then(entriesToComponentDefinitions)

    function foldersToCollections(folders) {

        const version = '1'

        return Promise.resolve(folders.map((folder) => {
            return {
                type: 'http://sbols.org/v2#Collection',
                displayId: 'ice_folder_' + folder.id,
                version: '1',
                uri: '/public/' + remoteConfig.id + '/ice_folder_' + folder.id + '/1',
                name: folder.folderName,
                description: folder.description
            }
        }))

    }

    function entriesToComponentDefinitions(entries) {

        console.log(JSON.stringify(entries))

        const version = '1'

        return Promise.resolve(entries.map((entry) => {
            
            return {
                type: 'http://sbols.org/v2#ComponentDefinition',
                uri: '/public/' + remoteConfig.id + '/' + displayId + '/' + version,
                displayId: displayId,
                version: version,
                name: entry.name,
                description: entry.shortDescription
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

    const folderId = parseInt(displayId.split('ice_folder_')[1])

    return ice.getFolder(remoteConfig, folderId).then((folder) => {

        return Promise.resolve({
            type: 'http://sbols.org/v2#Collection',
            displayId: 'ice_folder_' + folder.id,
            version: '1',
            uri: '/public/' + remoteConfig.id + '/ice_folder_' + folder.id + '/1',
            name: folder.folderName,
            description: folder.description
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


