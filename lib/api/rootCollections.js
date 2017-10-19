
const { getRootCollectionMetadata } = require('../query/collection')

function rootCollections(req, res) {

    getRootCollectionMetadata(null,req.user).then((collections) => {

	console.log('getRoot : '+JSON.stringify(req.user))
	if (req.user) {
	    return Promise.all([collections,getRootCollectionMetadata(req.user.graphUri,req.user)])
	} else {
	    return Promise.all([collections])
	}

    }).then((results) => {

	var collections = []
	results.forEach((result) => {
	    collections = collections.concat(result)
	})

        res.header('content-type', 'application/json').send(JSON.stringify(collections))

    }).catch((err) => {

        res.status(500).send(err.stack)

    })



}

module.exports = rootCollections

