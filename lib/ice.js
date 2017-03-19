
const SBOLDocument = require('sboljs')

const request = require('request')

const config = require('./config')

function getIceJson(remoteConfig, path) {

    var retriesLeft = config.get('iceMaxRetries')

    return attempt()

    function attempt() {

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

                if(response.statusCode === 500) {

                    if(-- retriesLeft < 0) {

                        reject(new Error('ICE returned 500 and ran out of retries'))
                        return
                    }

                    console.log('Got a 500; ' + retriesLeft + ' retries left...')

                    setTimeout(() => {
                        resolve(attempt())
                    }, config.get('iceRetryDelay'))

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
            url: remoteConfig.url + '/rest/file/' + partNum + '/sequence/sbol2'
        }, (err, response, body) => {

            if(err) {
                reject(err)
                return
            }

            if(response.statusCode >= 300) {
                reject(new Error('HTTP ' + response.statusCode))
                return
            }

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
                .then((folder) => Promise.resolve(folder.entries))

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

