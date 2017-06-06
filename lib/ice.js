
const SBOLDocument = require('sboljs')

const request = require('request')

const config = require('./config')

const extend = require('xtend')

var serializeSBOL = require('./serializeSBOL')

function getIceJson(remoteConfig, path, qs) {

    var retriesLeft = config.get('iceMaxRetries')?config.get('iceMaxRetries'):3

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

                console.log('getIceJson: success ' + remoteConfig.url + path)
		if (body) {
                    resolve(JSON.parse(body))
		} else {
		    resolve('{}')
		}
            })

        })

    }

}

function postIceFile(remoteConfig, path, sbol, id) {

    var retriesLeft = 0 //config.get('iceMaxRetries')?config.get('iceMaxRetries'):3

    return attempt()

    function attempt() {


        return new Promise((resolve, reject) => {

            console.log('postIceFile: ' + remoteConfig.url + path)
            console.log('formData: {\"file\": ... ,\"entryRecordId\": ' +id + ', \"entryType\": \"PART\" }')

	    request({
                method: 'post',
                headers: {
		    'Content-Type': 'multipart/form-data; charset=UTF-8',
                    'X-ICE-API-Token-Client': remoteConfig['X-ICE-API-Token-Client'],
                    'X-ICE-API-Token': remoteConfig['X-ICE-API-Token'],
                    'X-ICE-API-Token-Owner': remoteConfig['X-ICE-API-Token-Owner'],
                },
		//files: { "file": "/Users/myers/Downloads/BBa_B0015.xml", 'Content-Type': 'multipart/form-data; charset=UTF-8 },
		formData: { "file": serializeSBOL(sbol).toString('utf8'), "entryRecordId": id, "entryType": "PART" },
		rejectUnauthorized: remoteConfig['rejectUnauthorized'],
                url: remoteConfig.url + path
                //url: 'https://httpbin.org/post' 
            }, (err, response, body) => {

                console.log('postIceFile: ' + remoteConfig.url + path + ': response received')

                if(err) {
                    console.log('postIceFile: error')
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
                    }, config.get('postRetryDelay'))

                    return
                }

                if(response.statusCode >= 300) {
                    //console.log(body)
                    reject(new Error('HTTP ' + response.statusCode))
                    return
                }

                console.log('postIceFile: success')
		console.log('SEQ:'+body)
                resolve(JSON.parse(body))
            })

        })

    }

}

function postIceJson(remoteConfig, path, post) {

    var retriesLeft = 0 //config.get('iceMaxRetries')?config.get('iceMaxRetries'):3

    return attempt()

    function attempt() {


        return new Promise((resolve, reject) => {

            console.log('postIceJson: ' + remoteConfig.url + path)
	    console.log('post: '+JSON.stringify(post))
            
	    request({
                method: 'post',
                headers: {
		    'Content-Type': 'application/json',
                    'X-ICE-API-Token-Client': remoteConfig['X-ICE-API-Token-Client'],
                    'X-ICE-API-Token': remoteConfig['X-ICE-API-Token'],
                    'X-ICE-API-Token-Owner': remoteConfig['X-ICE-API-Token-Owner'],
                },
		//files: { "file": "/Users/myers/Downloads/BBa_B0015.xml", 'Content-Type': 'multipart/form-data; charset=UTF-8 },
		json: post,
		rejectUnauthorized: remoteConfig['rejectUnauthorized'],
                url: remoteConfig.url + path
                //url: 'https://httpbin.org/post' 
            }, (err, response, body) => {

                console.log('postIceJson: ' + remoteConfig.url + path + ': response received')

                if(err) {
                    console.log('postIceJson: error')
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
                    }, config.get('postRetryDelay'))

                    return
                }

                if(response.statusCode >= 300) {
                    //console.log(body)
                    reject(new Error('HTTP ' + response.statusCode))
                    return
                }

                console.log('postIceJson: success')
		console.log('resp:'+response.statusCode)
		console.log('body:'+JSON.stringify(body))
                resolve(body)
            })

        })

    }

}

function getPart(remoteConfig, partId) {

    return getIceJson(remoteConfig, '/rest/parts/' + partId)

}

function createSequence(remoteConfig, sbol, name, description) {

    var part = {
	type: 'PART',
	name: name,
	//alias: String,
	//keywords: String,
	status: 'Complete',
	shortDescription: description,
	longDescription: '',
	//references: String,
	bioSafetyLevel: 0,
	//intellectualProperty: String,
	//links: [String],
	principalInvestigator: remoteConfig['PI'],
	principalInvestigatorEmail: remoteConfig['PIemail'],
	//selectionMarkers: [String],
	fundingSource: '',
	//parameters: [{name: String, value: String}],
    }
    return postIceJson(remoteConfig, '/rest/parts', part).then((result) => {
	var id = parseInt(result.id)
	return postIceFile(remoteConfig, '/rest/file/sequence/', sbol, id).then((result) => {
	    var entryId = parseInt(result.entryId)
	    var post = {}
	    post.type = 'WRITE_ENTRY'
	    post.article = 'GROUP'
	    post.articleId = remoteConfig['groupId']
	    return postIceJson(remoteConfig, '/rest/parts/' + entryId + '/permissions/', post)
	})
    })
}

function getSequence(remoteConfig, partNum) {

    console.log('getIceSequence: ' + remoteConfig.url + '/rest/file/' + partNum + '/sequence/sbol2')

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
		console.log('getIceSequence: ERROR ' + err + ' : ' + remoteConfig.url + '/rest/file/' + partNum + '/sequence/sbol2')
                reject(err)
                return
            }

            if(response.statusCode >= 300) {
		console.log('getIceSequence: ERROR ' + response.statusCode + ' : ' + remoteConfig.url + '/rest/file/' + partNum + '/sequence/sbol2')
                reject(new Error('HTTP ' + response.statusCode))
                return
            }

            SBOLDocument.loadRDF(body, (err, sbol) => {

                if(err) {
		    console.log('getIceSequence: ERROR ' + err + ' : ' + remoteConfig.url + '/rest/file/' + partNum + '/sequence/sbol2') 
                    reject(err)
                } else {
		    console.log('getIceSequence: success' + ' : ' + remoteConfig.url + '/rest/file/' + partNum + '/sequence/sbol2')
                    resolve(sbol)
		}

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
    limit = limit || 1000000

    return getIceJson(remoteConfig, '/rest/collections/' + remoteConfig.iceCollection + '/folders')
        .then((folders) => {
	    if (folders.length > offset + limit) {
		return Promise.resolve(folders.slice(offset, offset + limit))
	    } else {
		return Promise.resolve(folders.slice(offset, folders.length))
	    }
	})

}

function getRootFolderEntryCount(remoteConfig) {

    return getIceJson(remoteConfig, '/rest/collections/AVAILABLE/entries')
                .then((folders) => Promise.resolve(folders.resultCount))

}

function getRootFolderEntries(remoteConfig, offset, limit) {

    offset = offset || 0
    limit = limit || 1000000

    return getIceJson(remoteConfig, '/rest/collections/AVAILABLE/entries?limit='+limit+'&'+'offset='+offset)
        .then((folders) => Promise.resolve(folders.data))

}

function getFolderEntryCount(remoteConfig, folderId) {

    offset = 0
    limit = 1000000

    return getIceJson(remoteConfig, '/rest/folders/' + folderId + '/entries?limit='+limit+'&'+'offset='+offset)
                .then((folder) => Promise.resolve(folder.entries.length))

}

function getFolderEntries(remoteConfig, folderId, offset, limit) {

    offset = offset || 0
    limit = limit || 1000000

    return getIceJson(remoteConfig, '/rest/folders/' + folderId + '/entries?limit='+limit+'&'+'offset='+offset)
        .then((folder) => Promise.resolve(folder.entries))

}

function getFolder(remoteConfig, folderId) {

    return getIceJson(remoteConfig, '/rest/folders/' + folderId)
        
}


module.exports = {
    getIceJson: getIceJson,
    getPart: getPart,
    getSequence: getSequence,
    createSequence: createSequence,
    getRootFolderCount: getRootFolderCount,
    getRootFolders: getRootFolders,
    getRootFolderEntryCount: getRootFolderEntryCount,
    getRootFolderEntries: getRootFolderEntries,
    getFolderEntryCount: getFolderEntryCount,
    getFolderEntries: getFolderEntries,
    getFolder: getFolder
}

