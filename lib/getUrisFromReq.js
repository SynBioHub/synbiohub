
const config = require('../config')

function getUrisFromReq(req) {

    var graphUris = []
    var uri
    var designId

    if(req.params.userId) {

        if(req.user && req.user.graphUri) {
            graphUris.push(req.user.graphUri)
        }

        designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
        uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId

    } else  {

        graphUris.push(null)

        designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
        uri = config.get('databasePrefix') + 'public/' + designId
    } 

    return {
        graphUris: graphUris,
        uri: uri,
        designId: designId
    }
}

module.exports = getUrisFromReq

