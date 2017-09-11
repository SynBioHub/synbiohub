const pug = require('pug')
const sparql = require('../sparql/sparql');
const loadTemplate = require('../loadTemplate')
const config = require('../config')
const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')
const getOwnedBy = require('../query/ownedBy')
const retrieveUris = require('../retrieveUris')

module.exports = function (req, res) {

    const uri = req.body.uri;
    const userUri = req.body.user;

    const graphUri = getGraphUriFromTopLevelUri(uri, req.user);

    var d = new Date();
    var modified = d.toISOString();

    modified = modified.substring(0, modified.indexOf('.'));
    modified = JSON.stringify(modified);

    const sharedAdditionQuery = loadTemplate('./sparql/AddToSharedCollection.sparql', {
        uri: uri,
        userUri: userUri
    });

    console.log(sharedAdditionQuery);

    return sparql.updateQuery(sharedAdditionQuery, userUri).then(() => {
        return retrieveUris(uri, graphUri);
    }).then(uris => {
        console.log(uris);

        let chunks = [];
        let offset = config.get('resolveBatch');

        for(let i = 0; i < uris.length; i += offset) {
            let end = i + offset < uris.length ? i + offset : uris.length;

            chunks.push(uris.slice(i, end));
        }

        return Promise.all(chunks.map(chunk => {

            let uris = chunk.map(uri => {
                return '<' + uri + '> sbh:ownedBy <' + userUri + '>';
            }).join(' . \n');

            const updateQuery = loadTemplate('./sparql/AddOwnedBy.sparql', {
                uris: uris
            });

            console.log(updateQuery);

            return sparql.updateQuery(updateQuery, graphUri);

        }));

    });
}

