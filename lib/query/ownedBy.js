
const sparql = require('../sparql/sparql')

const loadTemplate = require('../loadTemplate')

function getOwnedBy(topLevelUri, graphUri) {

    const query = loadTemplate('./sparql/GetOwnedBy.sparql', {
        topLevel: topLevelUri
    })

    return sparql.queryJson(query, graphUri).then((results) => {

        return Promise.resolve(results.map((result) => result.ownedBy))

    })


}

module.exports = getOwnedBy

