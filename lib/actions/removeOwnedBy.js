const retrieveUris = require('../retrieveUris');
const config = require('../config');
const sparql = require('../sparql/sparql');
const getUrisFromReq = require('../getUrisFromReq');
const loadTemplate = require('../loadTemplate');

module.exports = function(req, res) {

    const {graphUri, uri, designId, share } = getUrisFromReq(req, res);

    return retrieveUris(uri, graphUri).then(uris => {

        let chunks = [];
        let offset = config.get('resolveBatch');

        for(let i = 0; i < uris.length; i += offset) {
            let end = i + offset < uris.length ? i + offset : uris.length;

            chunks.push(uris.slice(i, end));
        }

        return Promise.all(chunks.map(chunk => {

            console.log(chunk)

            let uris = chunk.map(uri => {
                console.log(uri);
                return '<' + uri + '> sbh:ownedBy <' + req.body.userUri + '>';
            }).join(' . \n');

            const updateQuery = loadTemplate('./sparql/RemoveOwnedBy.sparql', {
                uris: uris
            });

            return sparql.updateQuery(updateQuery, graphUri).then(() => {

                console.log(share);
                res.redirect(share);
            });

        }));

    });

}
