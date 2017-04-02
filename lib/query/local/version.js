
const loadTemplate = require('../../loadTemplate')
const sparql = require('../../sparql/sparql')
const semver = require('semver')

function getVersion(uri, graphUri) {

    var query = loadTemplate('./sparql/GetVersions.sparql', {
        uri: uri
    })

    return sparql.queryJson(query, graphUri).then((results) => {

        if(results && results[0]) {
	    var latestVersion = results[0].version
	    results.forEach(function(result) {
		var version = result.version
		if (!semver.valid(version)) {
		    version = version + '.0'
		}
		if (!semver.valid(version)) {
		    version = version + '.0'
		}
		var latest = latestVersion
		if (!semver.valid(latest)) {
		    latest = latest + '.0'
		}
		if (!semver.valid(latest)) {
		    latest = latest + '.0'
		}
		if (semver.gt(version,latest)) {
		    latestVersion = result.version
		}
	    })
	    return Promise.resolve(latestVersion)

        } else {

            return Promise.reject('not found')

        }

    })

}

module.exports = {
    getVersion: getVersion
}

