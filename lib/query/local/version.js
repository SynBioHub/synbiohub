
const loadTemplate = require('../../loadTemplate')
const sparql = require('../../sparql/sparql')
const compareMavenVersions = require('../../compareMavenVersions')

function getVersion(uri, graphUri) {

    var query = loadTemplate('./sparql/GetVersions.sparql', {
        uri: uri
    })

    return sparql.queryJson(query, graphUri).then((results) => {

        if(results && results[0]) {

            const sortedVersions = results.sort((a, b) => {

                return compareMavenVersions(a.version, b.version)

            }).reverse()

            return Promise.resolve(sortedVersions[0].version)

        } else {

            return Promise.reject('not found')

        }

    })

}

module.exports = {
    getVersion: getVersion
}

