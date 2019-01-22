
const request = require('request')
const config = require('../../../config')

const { collateArrays } = require('../../collate')

function getCollectionMemberCount (remoteConfig, uri) {
  return new Promise((resolve, reject) => {
    request({
      method: 'get',
      url: remoteConfig.url + '/' + uri.slice(config.get('databasePrefix').length) + '/memberCount'
    }, (err, res, body) => {
      if (err) {
        reject(err)
        return
      }

      if (res.statusCode >= 300) {
        console.log(body)
        reject(new Error('HTTP ' + res.statusCode))
        return
      }

      resolve(JSON.parse(body))
    })
  })
}

function getRootCollectionMetadata (remoteConfig) {

}

function getContainingCollections (remoteConfig, uri) {
  return Promise.resolve([])
}

function getCollectionMembers (remoteConfig, uri, limit, offset) {
  return new Promise((resolve, reject) => {
    request({
      method: 'get',
      url: remoteConfig.url + '/' + uri.slice(config.get('databasePrefix').length) + '/members'
    }, (err, res, body) => {
      if (err) {
        reject(err)
        return
      }

      if (res.statusCode >= 300) {
        console.log(body)
        reject(new Error('HTTP ' + res.statusCode))
        return
      }

      const members = JSON.parse(body)

      members.forEach((member) => {
        member.uri = member.uri.slice(remoteConfig.url.length)
      })

      resolve(members)
    })
  })
}

function getCollectionMetaData (remoteConfig, uri) {
  return new Promise((resolve, reject) => {
    request({
      method: 'get',
      url: remoteConfig.url + '/' + uri.slice(config.get('databasePrefix').length) + '/metadata'
    }, (err, res, body) => {
      if (err) {
        reject(err)
        return
      }

      if (res.statusCode >= 300) {
        console.log(body)
        reject(new Error('HTTP ' + res.statusCode))
        return
      }

      resolve(JSON.parse(body))
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
