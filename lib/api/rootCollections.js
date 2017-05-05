
const { getRootCollectionMetadata } = require('../query/collection')

function rootCollections(req, res) {

    getRootCollectionMetadata(null,req.user).then((collections) => {

        res.header('content-type', 'application/json').send(JSON.stringify(collections))

    }).catch((err) => {

        res.status(500).send(err.stack)

    })



}

module.exports = rootCollections

