const pug = require('pug');
const config = require('../config');
const loadTemplate = require('../loadTemplate');
const db = require('../db');
const { queryJson } = require('../sparql/sparql');

module.exports = function (req, res) {
    let databasePrefix = config.get('databasePrefix');
    let userUri = databasePrefix + 'user/' + req.user.username;
    let values = {
        userUri: userUri
    };

    db.model.User.findAll().then(users => {

        let graphs = [];

        users.forEach(user => {
            if (user.id != req.user.id) {
                graphs.push(user.graphUri);
            }
        });

        // FIND OUT IF WE SHOULD SEARCH THE PUBLIC GRAPH.
        // graphs.push(null);

        return graphs;

    }).then(graphs => {

        let sparqlQuery = loadTemplate('sparql/findOwnedBy.sparql', values);

        return Promise.all(graphs.map(graph => {
            return queryJson(sparqlQuery, graph);
        }))

    }).then(results => {
        let collated = [];

        results.forEach(graphResults => {
            graphResults.forEach(result =>{ 
                result.uri = result.subject;
                result.url = '/' + result.uri.toString().replace(databasePrefix, '');
                
                delete result.subject;

                collated.push(result);
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