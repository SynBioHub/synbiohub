
const SBOLDocument = require('sboljs')

const request = require('request')

const config = require('./config')

const extend = require('xtend')

function getBenchlingJson(remoteConfig, path, qs) {

    console.log('getBenchlingJson:'+ path)

    var retriesLeft = config.get('benchlingMaxRetries')

    return attempt()

    function attempt() {


        return new Promise((resolve, reject) => {

            console.log('getBenchlingJson: ' + remoteConfig.url + path)

            request({
                method: 'get',
                headers: { },
		'auth': {
		    'username': remoteConfig['X-BENCHLING-API-Token']
		},
                qs: qs || {},
		rejectUnauthorized: remoteConfig['rejectUnauthorized'],
                url: remoteConfig.url + path
            }, (err, response, body) => {

                console.log('getBenchlingJson: ' + remoteConfig.url + path + ': response received')

                if(err) {
                    console.log('getBenchlingJson: error')
                    reject(err)
                    return
                }

                if(response.statusCode === 500) {

                    if(-- retriesLeft < 0) {

                        reject(new Error('Benchling returned 500 and ran out of retries'))
                        return
                    }

                    console.log('Got a 500; ' + retriesLeft + ' retries left...')

                    setTimeout(() => {
                        resolve(attempt())
                    }, config.get('benchlingRetryDelay'))

                    return
                }

                if(response.statusCode >= 300) {
                    //console.log(body)
                    reject(new Error('HTTP ' + response.statusCode))
                    return
                }

                console.log('getBenchlingJson: success')
                resolve(JSON.parse(body))
            })

        })

    }

}

function getPart(remoteConfig, partId) {

    console.log('getPart:'+partId)

    return getBenchlingJson(remoteConfig, '/sequences/' + partId)

}

function getSequence(remoteConfig, partId) {

    console.log('getSequence:'+partId)

    return getBenchlingJson(remoteConfig, '/sequences/' + partId)

}

function getRootFolderCount(remoteConfig) {

    console.log('getRootFolderCount')

    return getBenchlingJson(remoteConfig, '/folders/')
                .then((folders) => Promise.resolve(folders.folders.length))

}

function getRootFolders(remoteConfig, offset, limit) {

    console.log('getRootFolders:' + ' offset='+offset+' limit='+limit)

    offset = offset || 0
    limit = limit || 1000000

    return getBenchlingJson(remoteConfig, '/folders/')
        .then((folders) => {
	    if (folders.folders.length > offset + limit) {
		return Promise.resolve(folders.folders.slice(offset, offset + limit))
	    } else {
		return Promise.resolve(folders.folders.slice(offset, folders.folders.length))
	    }
	})

}

function getFolderEntryCount(remoteConfig, folderId) {

    console.log('getFolderEntryCount:'+folderId)

    offset = 0
    limit = 1000000

    return getBenchlingJson(remoteConfig, '/folders/' + folderId) // + '/entries?limit='+limit+'&'+'offset='+offset)
                .then((folder) => Promise.resolve(folder.count))

}

function getFolderEntries(remoteConfig, folderId, offset, limit) {

    console.log('getFolderEntries:'+folderId)

    offset = offset || 0
    limit = limit || 1000000

    return getBenchlingJson(remoteConfig, '/folders/' + folderId) // + '/entries?limit='+limit+'&'+'offset='+offset)
        .then((folder) => {
	    if (folder.sequences.length > offset + limit) {
		return Promise.resolve(folder.sequences.slice(offset, offset + limit))
	    } else {
		return Promise.resolve(folder.sequences.slice(offset, folder.sequences.length))
	    }
	})
}

function getFolder(remoteConfig, folderId) {

    console.log('getFolder:'+folderId)

    return getBenchlingJson(remoteConfig, '/folders/' + folderId)
        
}


module.exports = {
    getBenchlingJson: getBenchlingJson,
    getPart: getPart,
    getSequence: getSequence,
    getRootFolderCount: getRootFolderCount,
    getRootFolders: getRootFolders,
    getFolderEntryCount: getFolderEntryCount,
    getFolderEntries: getFolderEntries,
    getFolder: getFolder
}

