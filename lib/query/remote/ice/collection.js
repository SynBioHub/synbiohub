
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
            uri: config.get('databasePrefix') + 'public/' + remoteConfig.id +
                        '/' + remoteConfig.rootCollection.displayId
                            + '/current',
            version: 'current',
            name: remoteConfig.rootCollection.name,
            displayId: remoteConfig.rootCollection.displayId,
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

        return ice.getRootFolders(remoteConfig, offset, limit).then(foldersToCollections)
    }

    const folderId = parseInt(displayId.slice(remoteConfig.folderPrefix.length))

    return ice.getFolderEntries(remoteConfig, folderId, offset, limit).then(entriesToMetadata).then(concatArrays)

    function concatArrays(arrs) {

        const res = []

        arrs.forEach((arr) => {
            Array.prototype.push.apply(res, arr)
        })

        return Promise.resolve(res)
    }

    function foldersToCollections(folders) {

        return Promise.resolve(folders.map((folder) => {
            return {
                type: 'http://sbols.org/v2#Collection',
                displayId: remoteConfig.folderPrefix + folder.id,
                version: 'current',
                uri: config.get('databasePrefix') + 'public/' + remoteConfig.id + '/' + remoteConfig.folderPrefix + folder.id + '/current',
                name: folder.folderName,
                description: folder.description,
                wasDerivedFrom: remoteConfig.url + '/folders/' + folder.id,
                remote: true
            }
        }))

    }

    function entriesToMetadata(entries) {

        console.log(JSON.stringify(entries))

        const version = 'current'

        return Promise.resolve(entries.map((entry) => {

            const res = [
                {
                    type: 'http://sbols.org/v2#ComponentDefinition',
                    uri: config.get('databasePrefix') + 'public/' + remoteConfig.id + '/' + entry.partId + '/' + version,
                    displayId: entry.partId,
                    version: version,
                    name: entry.name,
                    description: entry.shortDescription,
                    wasDerivedFrom: remoteConfig.url + '/entry/' + entry.partId,
                    remote: true
                }
            ]

            if(entry.hasSequence) {

                res.push({
                    type: 'http://sbols.org/v2#Sequence',
                    uri: config.get('databasePrefix') + remoteConfig.id + '/' + entry.partId + remoteConfig.sequenceSuffix + '/' + version,
                    displayId: entry.partId + remoteConfig.sequenceSuffix,
                    version: version,
                    name: entry.name + ' sequence',
                    description: '',
                    wasDerivedFrom: remoteConfig.url + '/entry/' + entry.partId,
                    remote: true
                })
            }

            return res
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
            uri: config.get('databasePrefix') + remoteConfig.id + '/' + remoteConfig.folderPrefix + folder.id + '/current',
            name: folder.folderName,
            description: folder.description,
            wasDerivedFrom: remoteConfig.url + '/folders/' + folder.id,
            remote: true
        })

    })

}

function getSubCollections(remoteConfig, uri) {

    const { displayId } = splitUri(uri)

    if(displayId === remoteConfig.rootCollection.displayId) {

        return getCollectionMembers(remoteConfig, uri, 0, 9999)

    } else {

        return Promise.resolve([])

    }

}

module.exports = {
    getCollectionMemberCount: getCollectionMemberCount,
    getRootCollectionMetadata: getRootCollectionMetadata,
    getContainingCollections: getContainingCollections,
    getCollectionMembers: getCollectionMembers,
    getCollectionMetaData: getCollectionMetaData,
    getSubCollections: getSubCollections
}


