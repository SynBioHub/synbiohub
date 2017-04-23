
const SBOLDocument = require('sboljs')

const request = require('request')

const config = require('./config')

const extend = require('xtend')

function getIceJson(remoteConfig, path, qs) {

    var retriesLeft = config.get('iceMaxRetries')

    return attempt()

    function attempt() {


        return new Promise((resolve, reject) => {

            console.log('getIceJson: ' + remoteConfig.url + path)

            request({
                method: 'get',
                headers: {
                    'X-ICE-API-Token-Client': remoteConfig['X-ICE-API-Token-Client'],
                    'X-ICE-API-Token': remoteConfig['X-ICE-API-Token'],
                    'X-ICE-API-Token-Owner': remoteConfig['X-ICE-API-Token-Owner'],
                },
                qs: qs || {},
		rejectUnauthorized: remoteConfig['rejectUnauthorized'],
                url: remoteConfig.url + path
            }, (err, response, body) => {

                console.log('getIceJson: ' + remoteConfig.url + path + ': response received')

                if(err) {
                    console.log('getIceJson: error')
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
                    //console.log(body)
                    reject(new Error('HTTP ' + response.statusCode))
                    return
                }

                console.log('getIceJson: success')
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
                'X-ICE-API-Token-Client': remoteConfig['X-ICE-API-Token-Client'],
                'X-ICE-API-Token': remoteConfig['X-ICE-API-Token'],
                'X-ICE-API-Token-Owner': remoteConfig['X-ICE-API-Token-Owner'],
            },
	    rejectUnauthorized: remoteConfig['rejectUnauthorized'],
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

    return getIceJson(remoteConfig, '/rest/collections/' + remoteConfig.iceCollection + '/folders')
                .then((folders) => Promise.resolve(folders.length))

}

function getRootFolders(remoteConfig, offset, limit) {

    offset = offset || 0
    limit = limit || Number.MAX_SAFE_INTEGER

    return getIceJson(remoteConfig, '/rest/collections/' + remoteConfig.iceCollection + '/folders')
        .then((folders) => {
	    if (folders.length > offset + limit) {
		return Promise.resolve(folders.slice(offset, offset + limit))
	    } else {
		return Promise.resolve(folders)
	    }
	})

}

function getRootFolderEntryCount(remoteConfig) {

    return getIceJson(remoteConfig, '/rest/collections/' + remoteConfig.iceCollection + '/entries')
                .then((folders) => Promise.resolve(folders.length))

}

function getRootFolderEntries(remoteConfig, offset, limit) {

    offset = offset || 0
    limit = limit || Number.MAX_SAFE_INTEGER

    return getIceJson(remoteConfig, '/rest/collections/' + remoteConfig.iceCollection + '/entries')
        .then((folders) => {
	    if (folders.length > offset + limit) {
		return Promise.resolve(folders.slice(offset, offset + limit))
	    } else {
		return Promise.resolve(folders)
	    }
	})

}

function getFolderEntryCount(remoteConfig, folderId) {

    return getIceJson(remoteConfig, '/rest/folders/' + folderId + '/entries')
                .then((folder) => Promise.resolve(folder.entries.length))

}

function getFolderEntries(remoteConfig, folderId, offset, limit) {

    offset = offset || 0
    limit = limit || Number.MAX_SAFE_INTEGER

    return getIceJson(remoteConfig, '/rest/folders/' + folderId + '/entries')
                .then((folder) => Promise.resolve(folder.entries.slice(offset, offset + limit)))

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
    getRootFolderEntryCount: getRootFolderEntryCount,
    getRootFolderEntries: getRootFolderEntries,
    getFolderEntryCount: getFolderEntryCount,
    getFolderEntries: getFolderEntries,
    getFolder: getFolder
}

