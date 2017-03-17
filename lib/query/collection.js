
const loadTemplate = require('../loadTemplate')
const config = require('../config')

const local = require('./local/collection')

const remote = {
    synbiohub: require('./remote/synbiohub/collection'),
    ice: require('./remote/ice/collection')
}

const splitUri = require('../splitUri')

const { collateArrays } = require('./collate')

function getCollectionMemberCount(uri, graphUri) {

    const { submissionId } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined ?
                remote[remoteConfig.type].getCollectionMemberCount(remoteConfig, uri) :
                local.getCollectionMemberCount(uri, graphUri)
}

function objValues(obj) {

    return Object.keys(obj).map((key) => obj[key])

}

function getRootCollectionMetadata(graphUri) {

    return Promise.all(
        [ local.getRootCollectionMetadata(graphUri) ].concat(
            objValues(config.get('remotes')).map((remoteConfig) => {
                return remote[remoteConfig.type].getRootCollectionMetadata(remoteConfig)
            })
        )
    ).then(collateArrays)
}

function getContainingCollections(uri, graphUri) {

    const { submissionId } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined ?
                remote[remoteConfig.type].getContainingCollections(remoteConfig, uri) :
                local.getContainingCollections(uri, graphUri)

}

function getCollectionMembers(uri, graphUri, limit, offset) {

    const { submissionId } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined ?
                remote[remoteConfig.type].getCollectionMembers(remoteConfig, uri, limit, offset) :
                local.getCollectionMembers(uri, graphUri, limit, offset)

}

function getCollectionMetaData(uri, graphUri) {

    const { submissionId } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined ?
                remote[remoteConfig.type].getCollectionMetaData(remoteConfig, uri) :
                local.getCollectionMetaData(uri, graphUri)
}

module.exports = {
    getRootCollectionMetadata: getRootCollectionMetadata,
    getCollectionMetaData: getCollectionMetaData,
    getCollectionMemberCount: getCollectionMemberCount,
    getContainingCollections: getContainingCollections,
    getCollectionMembers: getCollectionMembers
}

