
const SBOLDocument = require('sboljs')

const request = require('request')

function getIceJson(remoteConfig, path) {

    return new Promise((resolve, reject) => {

        console.log('getIceJson: ' + remoteConfig.url + path)

        request({
            method: 'get',
            headers: {
                'X-ICE-API-Token-Client': remoteConfig.clientID,
                'X-ICE-API-Token': remoteConfig.token
            },
            url: remoteConfig.url + path
        }, (err, response, body) => {

            if(err) {
                reject(err)
                return
            }

            if(response.statusCode >= 300) {
                console.log(body)
                reject(new Error('HTTP ' + response.statusCode))
                return
            }

            resolve(JSON.parse(body))
        })

    })

}

function getPart(remoteConfig, partId) {

    return getIceJson(remoteConfig, '/rest/parts/' + partId)

}

function getSequence(remoteConfig, partNum) {

    return new Promise((resolve, reject) => {

        request({
            method: 'get',
            headers: {
                'X-ICE-API-Token-Client': remoteConfig.clientID,
                'X-ICE-API-Token': remoteConfig.token
            },
            url: remote.url + '/rest/file/' + partNum + '/sequence/sbol2'
        }, (err, response, body) => {

            if(err) {
                reject(err)
                return
            }

            if(response.statusCode >= 300) {
                console.log(body)
                reject(new Error('HTTP ' + response.statusCode))
                return
            }

            console.log('--- body start')
            console.log(body)
            console.log('--- body end')

            SBOLDocument.loadRDF(body, (err, sbol) => {

                if(err)
                    reject(err)
                else
                    resolve(sbol)

            })

        })

    })

}

function getRootFolderCount(remoteConfig) {

    return getRootFolders(remoteConfig).then(
                (folders) => Promise.resolve(folders.length))

}

function getRootFolders(remoteConfig) {

    return getIceJson(remoteConfig, '/rest/collections/' + remoteConfig.iceCollection + '/folders')

}

function getFolderEntryCount(remoteConfig, folderId) {

    return getFolderEntries(remoteConfig, folderId)
                .then((entries) => Promise.resolve(entries.length))

}

function getFolderEntries(remoteConfig, folderId) {

    return getIceJson(remoteConfig, '/rest/folders/' + folderId + '/entries')

}

function getFolder(remoteConfig, folderId) {

    return getIceJson(remoteConfig, '/rest/folders/' + folderId)
        
}


module.exports = {
    getIceJson: getIceJson,
    getPart: getPart,
    getSequence: getSequence,
    getRootFolderCount: getRootFolderCount,
    getRootFolders: getRootFolders,
    getFolderEntryCount: getFolderEntryCount,
    getFolderEntries: getFolderEntries,
    getFolder: getFolder
}

