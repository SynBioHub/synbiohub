const pug = require('pug')
const sparql = require('../sparql/sparql')
const loadTemplate = require('../loadTemplate')
const config = require('../config')
const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')
const getOwnedBy = require('../query/ownedBy')

module.exports = function (req, res) {

    const uri = req.body.uri;
    const userUri = req.body.user;

    const graphUri = getGraphUriFromTopLevelUri(uri, req.user);

    var d = new Date();
    var modified = d.toISOString();

    modified = modified.substring(0, modified.indexOf('.'));
    modified = JSON.stringify(modified);

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
                return '<' + uri + '> sbh:ownedBy <' + userUri + '>';
            }).join(' . \n');

            const updateQuery = loadTemplate('./sparql/AddOwnedBy.sparql', {
                uris: uris
            });

            return sparql.updateQuery(updateQuery, graphUri);

        }));

    });
}

function retrieveUris(rootUri, graphUri) {

    let batchSize = config.get('resolveBatch');
    let resolved = [rootUri];
    let pending = [rootUri];

    return doNextQuery();

    function doNextQuery() {

        let resolveCount = pending.length >= batchSize ? batchSize : pending.length;

        let toResolve = pending.slice(0, resolveCount);
        pending = pending.slice(resolveCount);

        let uriFragment = toResolve.map(uri => {
            return '{ <' + uri + '> ?p ?object }';
        }).join(' UNION \n') + ' . ';

        let data = {
            uriFragments: uriFragment
        };

        let query = loadTemplate('sparql/FetchRelatedURIs.sparql', data);

        return sparql.queryJson(query, graphUri).then(results => {

            results.forEach(result => {
               let object = result["object"];

               if(object.indexOf(graphUri) !== -1 && resolved.indexOf(object) == -1 && object != graphUri) {
                    resolved.push(object);
                    pending.push(object);
               }
            });

            if(pending.length > 0) {
                return doNextQuery();
            } else {
                return resolved;
            }

        });
    }
}
