
var getCollection = require('../get-sbol').getCollection

var stack = require('../stack');

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var prefixify = require('../prefixify')

var pug = require('pug')

var base64 = require('../base64')

module.exports = function(req, res) {

	var locals = {
        section: 'collection',
        user: req.user
    }

    // todo check the prefix is real
    //req.params.prefix
    //req.params.designid

    var uri = base64.decode(req.params.collectionURI)

    console.log(uri)

    var sbol
    var collection

    var mappings = {}

    
    async.series([

        function retrieveSBOL(next) {

            var stores = [
                stack.getDefaultStore()
            ]

            if(req.userStore)
                stores.push(req.userStore)

            getCollection(null, uri, stores, function(err, _sbol, _collection) {

                if(err) {

                    res.status(500).send(err.stack)

                } else {

                    if(!_collection) {
                        return res.status(404).send('not found\n' + uri)
                    }

                    sbol = _sbol
                    collection = _collection

                    //meta = sbolmeta.summarizeCollection(collection)
                    next()
                }
            })

        },

        function renderView() {

            locals.meta = {
                uri: collection.uri + '',
                base64Uri: base64.encode(collection.uri + ''),
                name: collection.name,
                description: collection.description,
                purpose: collection.getAnnotation('http://synbiohub.org#purpose') || '',
                chassis: collection.getAnnotation('http://synbiohub.org#chassis') || '',
                triplestore: collection.store.storeUrl === stack.getDefaultStore().storeUrl ? 'public' : 'private',

                numComponents: collection.members.length,

                components: collection.members.map((member) => {

                    return {
                        name: member.name,
                        url: '/component/' + base64.encode(member.uri + '')
                    }

                })

            }

            locals.sbolUrl = '/collection/' + locals.meta.base64Uri + '.xml'
            locals.fastaUrl ='/collection/' + locals.meta.base64Uri + '.fasta'
            locals.keywords = []
            locals.citations = []

            res.send(pug.renderFile('templates/views/collection.jade', locals))
        }
    ])
	
};


