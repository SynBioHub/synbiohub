
var sha1 = require('sha1');

const config = require('./config')

var util = require('./util');

var pug = require('pug')

const splitUri = require('./splitUri')

function getUrisFromReq(req, res) {

    var graphUri
    var uri
    var designId
    var share
    var baseUri

    if(req.params.userId) {
        
        designId = req.params.collectionId + '/' + req.params.displayId
        if (req.params.version) {
            designId = designId + '/' + req.params.version
        }
        url = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId
        baseUri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + req.params.collectionId
        uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId

        var webOfRegistries = config.get('webOfRegistries')
        var prefix = config.get('databasePrefix')
        prefix = prefix.substring(0,prefix.length-1)
        share = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' + sha1('synbiohub_' + sha1(uri) + config.get('shareLinkSalt')) + '/share'
        if (webOfRegistries[prefix]) {
            share = share.replace(prefix,webOfRegistries[prefix])
        }

        graphUri = null
        if (req.user && req.user.graphUri) {
            graphUri = req.user.graphUri
        }

        if (req.params.hash) {
            if (sha1('synbiohub_' + sha1(uri) + config.get('shareLinkSalt'))===req.params.hash) {
                graphUri = config.get('databasePrefix') + util.createTriplestoreID(req.params.userId)
                url = share
            }
        }

    } else if (req.params.version && req.params.version === 'current') {

        graphUri = null

        designId = req.params.collectionId + '/' + req.params.displayId
        if (req.params.version) {
            designId = designId + '/' + req.params.version
        }
        url = '/public/' + designId
        baseUri = config.get('databasePrefix') + 'public/' + req.params.collectionId
        uri = config.get('databasePrefix') + 'public/' + designId
   
        const { submissionId, version } = splitUri(uri)
        const remoteConfig = config.get('remotes')[submissionId]

        if (remoteConfig && !remoteConfig.public && !(req.user && req.user.isMember)) {

            var locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ 'Permission Denied' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
        }

    } else  {

        graphUri = null

        designId = req.params.collectionId + '/' + req.params.displayId
        if (req.params.version) {
            designId = designId + '/' + req.params.version
        }
        url = '/public/' + designId
        baseUri = config.get('databasePrefix') + 'public/' + req.params.collectionId
        uri = config.get('databasePrefix') + 'public/' + designId
    } 

    return {
        graphUri: graphUri,
        uri: uri,
        designId: designId,
        share: share,
        url: url,
        baseUri: baseUri
    }
}

module.exports = getUrisFromReq

