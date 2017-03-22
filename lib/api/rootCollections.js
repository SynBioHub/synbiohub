
const { getRootCollectionMetadata } = require('../query/collection')

function rootCollections(req, callback) {

    getRootCollectionMetadata(null).then((collections) => {

        callback(null, 200, {
            mimeType: 'application/json',
            body: JSON.stringify(collections)
        })

    }).catch((err) => {

        callback(err)

    })



}

module.exports = rootCollections

