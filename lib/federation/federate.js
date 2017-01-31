
var config = require('../config')

var async = require('async')

var request = require('request')

function FederationMiddleware(endpoint, collate) {

    var otherStacks = config.get('otherStacks')

    return function federate(req, res) {

        var responses = []

        endpoint(req, onLocalResponse)
        
        function onLocalResponse(err, statusCode, localResponse) {

            if(err) {

                res.status(500).send(err.stack)
                return

            }

            if(statusCode >= 300) {

                if(statusCode !== 404) {

                    return res.status(statusCode).send(localResponse)

                }

            } else {

                responses.push(localResponse)

            }

            async.each(otherStacks, queryRemoteStack, onFederationComplete)
        }

        function queryRemoteStack(otherStackUrl, next) {

            console.log('[Federation] Querying remote stack: ' + otherStackUrl)

            request({

                method: req.method,
                uri: otherStackUrl + req.url

            }, (err, remoteResponse) => {

                console.log('[Federation] Received response')

                if(remoteResponse.statusCode >= 300) {

                    if(remoteResponse.statusCode === 404) {
                        next()
                    } else {
                        res.status(remoteResponse.statusCode).send(remoteResponse)
                    }

                } else {

                    responses.push(remoteResponse)
                    next()

                }
            })

        }

        function onFederationComplete(err) {

            if(err) {
                return res.status(500).send(err)
            }

            collate(responses, res)

        }
    }
}

module.exports = FederationMiddleware

