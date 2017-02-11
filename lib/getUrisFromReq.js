
var sha1 = require('sha1');

const config = require('./config')

var util = require('./util');

function getUrisFromReq(req) {

    var graphUris = []
    var uri
    var designId
    var share

    if(req.params.userId) {

        if(req.user && req.user.graphUri && !req.params.hash) {
            graphUris.push(req.user.graphUri)
        }

        designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	url = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId
        uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
	share = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' +
	    sha1('synbiohub_' + sha1(uri) + config.get('shareLinkSalt')) + '/share'

	if (req.params.hash) {
	    if (sha1('synbiohub_' + sha1(uri) + config.get('shareLinkSalt'))===req.params.hash) {
		graphUris.push(config.get('databasePrefix') + util.createTriplestoreID(req.params.userId))
		url = share
	    }
	}

    } else  {

        graphUris.push(null)

        designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	url = '/public/' + designId
        uri = config.get('databasePrefix') + 'public/' + designId
    } 

    return {
        graphUris: graphUris,
        uri: uri,
        designId: designId,
	share: share,
	url: url
    }
}

module.exports = getUrisFromReq

