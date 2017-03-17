
const request = require('request')
const config = require('../../../config')

function getType(remoteConfig, uri) {

    return new Promise((resolve, reject) => {

        request({
            method: 'get',
            url: remoteConfig.url + '/' + uri.slice(config.get('databasePrefix').length) + '/metadata'
        }, (err, res, body) => {

            if(err) {
                reject(err)
                return
            }

            console.log(body)

            resolve(JSON.parse(body).type)

        })

    })

}

module.exports = {
    getType: getType
}

