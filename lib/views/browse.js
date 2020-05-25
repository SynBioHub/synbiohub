const pug = require('pug')
const config = require('../config')
const { getRootCollectionMetadata } = require('../query/collection')
const uriToUrl = require('../uriToUrl')
const sha1 = require('sha1')

module.exports = function (req, res) {
  getRootCollectionMetadata(null, req.user)
    .then(collections => {
      const collectionIcons = config.get('collectionIcons')

      collections.forEach(collection => {
        console.log(config.get('databasePrefix') + collection.uri)

        collection.url = uriToUrl(collection.uri)

        if (req.url.endsWith('/share')) {
          collection.url += '/' + sha1('synbiohub_' + sha1(collection.uri) + config.get('shareLinkSalt')) + '/share'
        };

        collection.icon = collectionIcons[collection.uri]

        const remoteConfig = config.get('remotes')[collection.displayId.replace('_collection', '')]
        if (!remoteConfig || (remoteConfig && remoteConfig.public && collection.version === 'current')) {
          collection.public = true
        } else {
          collection.public = false
        }
      })

      const instances = config.get('instances')
      if (instances) {
        instances.forEach(instance => {
          collections.push({ uri: instance.uriPrefix,
            name: instance.name,
            description: instance.description,
            displayId: instance.name,
            version: '',
            url: instance.instanceUrl,
            public: true,
            remote: true
          })
        })
      }

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
      var locals = {
        config: config.get(),
        section: 'errors',
        user: req.user,
        errors: [err.stack]
      }

      res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    })
}
