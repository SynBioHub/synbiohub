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

    const updateQuery = loadTemplate('./sparql/AddOwnedBy.sparql', {
        topLevel: uri,
        user: userUri,
        modified: JSON.stringify(modified)
    })

    return getOwnedBy(uri, graphUri).then((ownedBy) => {

        if (ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
            res.status(401).send('not authorized to edit this submission')
            return
        }

        return sparql.updateQuery(updateQuery, graphUri);
    })
}