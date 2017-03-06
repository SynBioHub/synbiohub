
const config = require('../config')
const request = require('request')

const fs = require('mz/fs')

const sparqlResultsToArray = require('./sparql-results-to-array')

const Timer = require('../util/execution-timer')

function escapeSparqlIRI(uri) {

    return '<' + uri + '>';

}

function query(sparql, graphUri, accept) {

    const triplestoreConfig = config.get('triplestore')

    graphUri = graphUri || triplestoreConfig.defaultGraph

    //console.log('gui: ' + graphUri)
    //console.log(sparql)

    return new Promise((resolve, reject) => {

        request({

            method: 'get',
            url: triplestoreConfig.sparqlEndpoint,
            qs: {
                query: sparql,
                'default-graph-uri': graphUri
            },
            headers: {
                accept: accept
            }

        }, (err, res, body) => {

            if(err) {
                reject(err)
                return
            }

            if(res.statusCode >= 300) {
                reject(new Error(body))
                return
            }

            resolve({
                type: res.headers['content-type'],
                body: body
            })
        })

    })

}

function queryJson(sparql, graphUri) {

    const timer = Timer('sparql query')

    return query(sparql, graphUri, 'application/sparql-results+json').then(parseResult)

    function parseResult(res) {

        timer()

        var results = JSON.parse(res.body)

        return Promise.resolve(sparqlResultsToArray(results))

    }


}

function upload(graphUri, data, type) {

    const triplestoreConfig = config.get('triplestore')

    graphUri = graphUri || triplestoreConfig.defaultGraph

    return new Promise((resolve, reject) => {
	
        request({

            method: 'POST',
            url: triplestoreConfig.graphStoreEndpoint,
            qs: {
                'graph-uri': graphUri,
            },
            auth:  {
                user: triplestoreConfig.username,
                pass: triplestoreConfig.password,
                sendImmediately: false
            },
            headers: {
                'content-type': type
            },
            body: data

        }, (err, res, body) => {

            if(err) {
                reject(err)
                return
            }

            if(res.statusCode >= 300) {
                reject(new Error(body))
                return
            }

            resolve(body)
        })

    })

}

function uploadFile(graphUri, filename, type) {

    const triplestoreConfig = config.get('triplestore')

    graphUri = graphUri || triplestoreConfig.defaultGraph

    return new Promise((resolve, reject) => {

        console.log('sparql upload file: ' + filename)
        console.log('gui ' + graphUri)
	
        /* TODO: it would be very nice to stream the file to the request
         * instead of loading it all into memory.
         * unfortunately, with auth: { sendImmediately: false }, the file gets
         * streamed to the initial request (which will fail), and then when the
         * retry request with authentication happens requestjs sends content
         * length of 0.
         */
        fs.readFile(filename).then((contents) => {

            const req = request({

                method: 'POST',
                url: triplestoreConfig.graphStoreEndpoint,
                qs: {
                    'graph-uri': graphUri,
                },
                auth:  {
                    user: triplestoreConfig.username,
                    pass: triplestoreConfig.password,
                    sendImmediately: false
                },
                headers: {
                    'content-type': type
                },
                body: contents

            }, (err, res, body) => {

                if(err) {
                    reject(err)
                    return
                }

                console.log('uploadfile done; ' + res.statusCode)
                console.log(body)

                if(res.statusCode >= 300) {
                    reject(new Error(body))
                    return
                }

                resolve(body)
            })

        })


    })

}

module.exports = {

    escape: require('pg-escape'),
    escapeIRI: escapeSparqlIRI,
    query: query,
    queryJson: queryJson,
    upload: upload,
    uploadFile: uploadFile,
}


