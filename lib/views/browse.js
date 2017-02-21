

var pug = require('pug')

var search = require('../search')

var config = require('../config')

var loadTemplate = require('../loadTemplate')

var sparql = require('../sparql/sparql')

module.exports = function(req, res) {

    var query = loadTemplate('./sparql/RootCollectionMetadata.sparql', { });

    sparql.queryJson(query, req.params.store).then((sparqlResults) => {

       return Promise.resolve(
           sparqlResults.map(function(result) {
            return {
                uri: result['Collection'].replace(config.get('databasePrefix'),''),
                name: result['name'] || '',
                description: result['description'] || '',
                displayId: result['displayId'] || '',
                version: result['version'] || ''
            };
        })
       )

    }).then((collections) => {

        var locals = {
            section: 'browse',
            user: req.user,
            collections: collections
        }

        res.send(pug.renderFile('templates/views/browse.jade', locals))
        
    }).catch((err) => {

        locals = {
            section: 'errors',
            user: req.user,
            errors: [ err.stack ]
        }

        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))


    })


}


