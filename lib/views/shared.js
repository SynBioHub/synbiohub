const pug = require('pug');
const config = require('../config');
const loadTemplate = require('../loadTemplate');
const db = require('../db');
const sparql = require('../sparql/sparql');
const getGraphUriFromTopLevevlUri = require('../getGraphUriFromTopLevelUri');
const sha1 = require('sha1');

module.exports = function (req, res) {
    let databasePrefix = config.get('databasePrefix');
    let userUri = databasePrefix + 'user/' + req.user.username;
    let values = {
        userUri: userUri
    };

    const sharedCollectionQuery = loadTemplate('./sparql/GetSharedCollection.sparql', values);

    sparql.queryJson(sharedCollectionQuery, req.user.graphUri).then(results => {
        
        return Promise.all(results.map(result => {
            let objectGraph = getGraphUriFromTopLevevlUri(result.object, req.user);
            let queryParameters = {
                uri: result.object
            };

            let metadataQuery = loadTemplate('./sparql/GetTopLevelMetadata.sparql', queryParameters);
            return sparql.queryJson(metadataQuery, objectGraph).then(result => {console.log(result); return result;});
        }))
        
    }).then(objects => {
        let collated = [];

        objects.forEach(array  => {
            array.forEach(object => {
                object.uri = object.persistentIdentity + '/' + object.version;
                object.url = '/' + object.uri.toString().replace(databasePrefix, '') + '/' + sha1('synbiohub_' + sha1(object.uri) + config.get('shareLinkSalt')) + '/share';

                delete object.object;
                collated.push(object);
            })
        })
        
        let locals = {
            config: config.get(),
            section: 'shared',
            user: req.user,
            searchResults: collated
        };

        res.send(pug.renderFile('templates/views/shared.jade', locals));
    })


};