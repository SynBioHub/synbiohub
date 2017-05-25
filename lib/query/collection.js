
const loadTemplate = require('../loadTemplate')
const config = require('../config')

const local = require('./local/collection')

const remote = {
    synbiohub: require('./remote/synbiohub/collection'),
    ice: require('./remote/ice/collection'),
    benchling: require('./remote/benchling/collection')
}

const splitUri = require('../splitUri')

const { collateArrays } = require('./collate')

function getCollectionMemberCount(uri, graphUri) {

    const { submissionId, version } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined && version === 'current' ?
                remote[remoteConfig.type].getCollectionMemberCount(remoteConfig, uri) :
                local.getCollectionMemberCount(uri, graphUri)
}

function objValues(obj) {

    return Object.keys(obj).map((key) => obj[key])

}

function getRootCollectionMetadata(graphUri,user) {

    return Promise.all(
        [ local.getRootCollectionMetadata(graphUri) ].concat(
            objValues(config.get('remotes')).map((remoteConfig) => {
		if (remoteConfig.public || (user && user.isMember)) {
                    return remote[remoteConfig.type].getRootCollectionMetadata(remoteConfig)
		} else {
		    return []
		}
            })
        )
    ).then(collateArrays)
}

function getContainingCollections(uri, graphUri) {

    const { submissionId, version } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined && version === 'current' ?
                remote[remoteConfig.type].getContainingCollections(remoteConfig, uri) :
                local.getContainingCollections(uri, graphUri)

}

function getCollectionMembers(uri, graphUri, limit, offset, sort, filter) {

    const { submissionId, version } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined && version === 'current' ?
                remote[remoteConfig.type].getCollectionMembers(remoteConfig, uri, limit, offset, sort, filter) :
                local.getCollectionMembers(uri, graphUri, limit, offset, sort, filter)

}

function getSubCollections(uri, graphUri) {

    const { submissionId, version } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined && version === 'current' ?
                remote[remoteConfig.type].getSubCollections(remoteConfig, uri) :
                local.getSubCollections(uri, graphUri)

}


function getCollectionMetaData(uri, graphUri) {

    const { submissionId, version } = splitUri(uri)
    const remoteConfig = config.get('remotes')[submissionId]

    return remoteConfig !== undefined && version === 'current' ?
                remote[remoteConfig.type].getCollectionMetaData(remoteConfig, uri) :
                local.getCollectionMetaData(uri, graphUri)
}

function getCollectionMembersRecursive(uri, graphUri) {

    return getCollectionMembers(uri, graphUri).then((members) => {

        const subCollections = members.filter((member) => {
            return member.type === 'http://sbols.org/v2#Collection'
        })

        return Promise.all(subCollections.map((subCollection) => {

            return getCollectionMembersRecursive(subCollection.uri, graphUri).then((members) => {

                subCollection.members = members

            })

        })).then(() => {

            return Promise.resolve(members)
        })

    })

}

module.exports = {
    getRootCollectionMetadata: getRootCollectionMetadata,
    getCollectionMetaData: getCollectionMetaData,
    getCollectionMemberCount: getCollectionMemberCount,
    getContainingCollections: getContainingCollections,
    getCollectionMembers: getCollectionMembers,
    getCollectionMembersRecursive: getCollectionMembersRecursive,
    getSubCollections: getSubCollections
}

