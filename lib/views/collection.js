
var getCollection = require('../get-sbol').getCollection

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var prefixify = require('../prefixify')

var pug = require('pug')

var base64 = require('../base64')

var getDisplayList = require('../getDisplayList')

var config = require('../config')

module.exports = function(req, res) {

    var stack = require('../stack')()

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

            function getDepth(componentDefinition) {

                var subComponents = componentDefinition.components

                var subInstanceDepths = subComponents.map((subComponent) => getDepth(subComponent.definition))

                return Math.max.apply(null, [ 0 ].concat(subInstanceDepths)) + 1
            }

            //var highestDepthComponent = collection.members[0]
            //var highestDepthComponentDepth = getDepth(collection.members[0])

            var members = collection.members.sort((a, b) => {
                
                return getDepth(b) - getDepth(a)

            })

            locals.meta = {
                uri: collection.uri + '',
                base64Uri: base64.encode(collection.uri + ''),
		id: collection.displayId,
                name: collection.name,
                description: collection.description,
                purpose: collection.getAnnotation('http://synbiohub.org#purpose') || '',
                chassis: collection.getAnnotation('http://synbiohub.org#chassis') || '',
                triplestore: collection.store.storeUrl === stack.getDefaultStore().storeUrl ? 'public' : 'private',

                numComponents: members.length,

                components: members.map((member) => {

                    return {
                        name: member.name?member.name:member.displayId,
                        url: '/'+member.uri.toString().replace(config.get('databasePrefix'),'')
                    }

                })

            }

            if(members.length > 0 && members[0].components.length > 0) {

                locals.meta.displayList = getDisplayList(members[0])

            }

            locals.sbolUrl = '/collection/' + locals.meta.base64Uri + '.xml'
            locals.fastaUrl ='/collection/' + locals.meta.base64Uri + '.fasta'
            locals.keywords = []
            locals.citations = []

            res.send(pug.renderFile('templates/views/collection.jade', locals))
        }
    ], function done(err) {

            res.status(500).send(err.stack)
                
    })
	
};


