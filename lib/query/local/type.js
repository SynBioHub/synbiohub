
const sparql = require('../../sparql/sparql')
const assert = require('assert')

function getType(uri, graphUri) {

    assert(!Array.isArray(graphUri))

    return sparql.queryJson('SELECT ?type WHERE { <' + uri + '> a ?type }', graphUri).then((result) => {

        if(result && result[0]) {

            return Promise.resolve(result[0].type)

        } else {

            return Promise.reject(new Error('getType: ' + uri + ' not found'))

        }

    })
}

module.exports = {
    getType: getType
}

