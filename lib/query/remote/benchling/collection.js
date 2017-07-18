
const request = require('request')
const config = require('../../../config')

const benchling = require('../../../benchling')

const splitUri = require('../../../splitUri')

function getCollectionMemberCount(remoteConfig, uri) {

    const { displayId } = splitUri(uri)

    if(displayId === remoteConfig.rootCollection.displayId) {

        return benchling.getRootFolderCount(remoteConfig)

    }

    if(displayId.indexOf(remoteConfig.folderPrefix) !== 0) {

        res.status(404).send('???')
        return

    }

    const folderId = displayId.slice(remoteConfig.folderPrefix.length)

    return benchling.getFolderEntryCount(remoteConfig, folderId)
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
            wasDerivedFrom: 'https://benchling.com', //remoteConfig.url + '/folders/',
            remote: true
        }
    ])

}

function getContainingCollections(remoteConfig, uri) {

    var rootUri = config.get('databasePrefix') + 'public/' + remoteConfig.id + '/' + remoteConfig.id + '_collection/current'
    
    if (uri != rootUri) {
	return Promise.resolve([{
	    uri: rootUri,
	    name: remoteConfig.rootCollection.name
	}])
    } else {
	return Promise.resolve([])
    }

}


function getCollectionMembers(remoteConfig, uri, limit, offset) {

    const { displayId } = splitUri(uri)

    if(displayId === remoteConfig.rootCollection.displayId) {

        return benchling.getRootFolders(remoteConfig, offset, limit).then(foldersToCollections)

    }

    const folderId = displayId.slice(remoteConfig.folderPrefix.length)

    return benchling.getFolderEntries(remoteConfig, folderId, offset, limit).then(entriesToMetadata).then(concatArrays)

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
                name: folder.name,
                description: folder.description,
                wasDerivedFrom: 'https://benchling.com', //remoteConfig.url + '/folders/' + folder.id,
                remote: true
	    }
	})).then((result) => { 
	    // result.unshift({
	    // 	type: 'http://sbols.org/v2#Collection',
	    // 	displayId: 'available',
	    // 	version: 'current',
	    // 	uri: config.get('databasePrefix') + 'public/' + remoteConfig.id + '/available/current',
	    // 	name: 'All Available Entries',
	    // 	description: 'Contains all available entries',
	    // 	wasDerivedFrom: remoteConfig.url + '/folders/available',
	    // 	remote: true
            // })
	    return Promise.resolve(result) 
	})

    }

    function entriesToMetadata(entries) {

        const version = 'current'

        return Promise.resolve(entries.map((entry) => {

            const res = [
                {
                    type: 'http://sbols.org/v2#ComponentDefinition',
                    uri: config.get('databasePrefix') + 'public/' + remoteConfig.id + '/' + entry.id + '/' + version,
                    displayId: entry.id,
                    version: version,
                    name: entry.name,
                    description: '',
                    wasDerivedFrom: 'https://benchling.com', //remoteConfig.url + '/sequences/' + entry.id,
                    remote: true
                }
            ]

	    // TODO: should we include sequences in the collection list?
            // if(entry.hasSequence) {

            //     res.push({
            //         type: 'http://sbols.org/v2#Sequence',
            //         uri: config.get('databasePrefix') + 'public/' + remoteConfig.id + '/' + entry.partId + remoteConfig.sequenceSuffix + '/' + version,
            //         displayId: entry.partId + remoteConfig.sequenceSuffix,
            //         version: version,
            //         name: entry.name + ' sequence',
            //         description: '',
            //         wasDerivedFrom: remoteConfig.url + '/entry/' + entry.partId,
            //         remote: true
            //     })
            // }

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

    const folderId = displayId.slice(remoteConfig.folderPrefix.length)

    return benchling.getFolder(remoteConfig, folderId).then((folder) => {

        return Promise.resolve({
            type: 'http://sbols.org/v2#Collection',
            displayId: remoteConfig.folderPrefix + folder.id,
            version: 'current',
            uri: config.get('databasePrefix') + remoteConfig.id + '/' + remoteConfig.folderPrefix + folder.id + '/current',
            name: folder.name,
            description: folder.description,
            wasDerivedFrom: 'https://benchling.com', //remoteConfig.url + '/folders/' + folder.id,
            remote: true
        })

    })

}

function getSubCollections(remoteConfig, uri) {

    const { displayId } = splitUri(uri)

    if(displayId === remoteConfig.rootCollection.displayId) {

        return getCollectionMembers(remoteConfig, uri)

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


