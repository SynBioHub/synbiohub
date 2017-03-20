

var pug = require('pug')

var search = require('../search')

var config = require('../config')

var loadTemplate = require('../loadTemplate')

var sparql = require('../sparql/sparql')

const { getRootCollectionMetadata } = require('../query/collection')

module.exports = function(req, res) {

    getRootCollectionMetadata(req.params.store)
        .then((collections) => {

        const collectionIcons = config.get('collectionIcons')

        collections.forEach((collection) => {

            console.log(config.get('databasePrefix') + collection.uri)

            collection.icon = collectionIcons[config.get('databasePrefix') + collection.uri]

        })

        var locals = {
            config: config.get(),
            section: 'browse',
            title: 'Browse Parts and Designs ‒ ' + config.get('instanceName'),
            metaDesc: 'Browse ' + collections.length + ' collection(s) including ' + collections.map((collection) => collection.name).join(', '),
            user: req.user,
            collections: collections
        }

        res.send(pug.renderFile('templates/views/browse.jade', locals))
        
    }).catch((err) => { 

        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err.stack ]
        }

        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))


    })


}


