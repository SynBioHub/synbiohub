var getCollectionMetaData = require('../get-sbol').getCollectionMetaData

var getCollectionMembers = require('../get-sbol').getCollectionMembers

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var sparql = require('../sparql-collate')

var prefixify = require('../prefixify')

var pug = require('pug')

var getDisplayList = require('../getDisplayList')

var config = require('../config')

module.exports = function(req, res) {

    var stack = require('../stack')()

	var locals = {
        section: 'collection',
        user: req.user
    }

    var designId
    var uri
    var collections = []

    console.log('coll='+req.params.collectionId+' disp='+req.params.displayId+' ver='+req.params.version)
    if(req.params.userId) {
	designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
    } else {
	designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	uri = config.get('databasePrefix') + 'public/' + designId
    } 
    console.log(uri)

    var sbol
    var collection

    var mappings = {}

    var metaData
    var members
    var storeUrl

    var stores = [
        stack.getDefaultStore()
    ]

    if(req.userStore)
        stores.push(req.userStore)

    
    async.series([

        function retrieveCollectionMetaData(next) {


            getCollectionMetaData(uri, stores, function(err, _metaData, _storeUrl) {

                if(err) {

                    res.status(500).send(err)

                } else {

		    metaData = _metaData[0]
		    storeUrl = _storeUrl
		    next()

		}
            })

        },

        function retrieveCollectionMembers(next) {

            getCollectionMembers(uri, stores, function(err, _members) {

                if(err) {

                    res.status(500).send(err)

                } else {

		    members = _members
		    next()

		}
            })

        },

        function lookupCollections(next) {

            var query =
                'PREFIX sbol2: <http://sbols.org/v2#>\n' +
                'PREFIX dcterms: <http://purl.org/dc/terms/>\n' +
            'SELECT ?subject ?displayId ?title WHERE {' +
                '   ?subject a sbol2:Collection .' +
                '   ?subject sbol2:member <' + uri + '> .' +
                '   OPTIONAL { ?subject sbol2:displayId ?displayId } .' +
                '   OPTIONAL { ?subject dcterms:title ?title } .' +
                '}'

            sparql(stores, query, (err, results) => {

                if(err) {

                    next(err)

                } else {

                    collections = results.map((result) => {
                        return {
                            uri: result.subject,
                            url: '/'+result.subject.toString().replace(config.get('databasePrefix'),''),
                            name: result.title?result.title:result.displayId
                        }
                    })

                    next()

                }
            })

        },

        function renderView(next) {

	    // TODO: this only makes sense when collection only includes component definitions
            // function getDepth(componentDefinition) {

            //     var subComponents = componentDefinition.components
		
	    // 	var subInstanceDepths = 0

	    // 	if (subComponents) {
            //         subInstanceDepths = subComponents.map((subComponent) => getDepth(subComponent.definition))
	    // 	}

            //     return Math.max.apply(null, [ 0 ].concat(subInstanceDepths)) + 1
            // }

            //var highestDepthComponent = collection.members[0]
            //var highestDepthComponentDepth = getDepth(collection.members[0])

	    var sbolNS = 'http://sbols.org/v2#'
            members.sort((a, b) => {
		if (a.type.endsWith(sbolNS+'Collection') && !b.type.endsWith(sbolNS+'Collection')) {
		    return -1
		} else if (b.type.endsWith(sbolNS+'Collection') && !a.type.endsWith(sbolNS+'Collection')) {
		    return 1
		} if (a.type.endsWith(sbolNS+'ModuleDefinition') && !b.type.endsWith(sbolNS+'ModuleDefinition')) {
		    return -1
		} else if (b.type.endsWith(sbolNS+'ModuleDefinition') && !a.type.endsWith(sbolNS+'ModuleDefinition')) {
		    return 1
		} if (a.type.endsWith(sbolNS+'Model') && !b.type.endsWith(sbolNS+'Model')) {
		    return -1
		} else if (b.type.endsWith(sbolNS+'Model') && !a.type.endsWith(sbolNS+'Model')) {
		    return 1
		} if (a.type.endsWith(sbolNS+'ComponentDefinition') && !b.type.endsWith(sbolNS+'ComponentDefinition')) {
		    return -1
		} else if (b.type.endsWith(sbolNS+'ComponentDefinition') && !a.type.endsWith(sbolNS+'ComponentDefinition')) {
		    return 1
		} if (a.type.endsWith(sbolNS+'Sequence') && !b.type.endsWith(sbolNS+'Sequence')) {
		    return -1
		} else if (b.type.endsWith(sbolNS+'Sequence') && !a.type.endsWith(sbolNS+'Sequence')) {
		    return 1
		} else {
                    return ((a.displayId < b.displayId) ? -1 : ((a.displayId > b.displayId) ? 1 : 0));
		}
            })

            locals.collections = collections

	    console.log(storeUrl)

            locals.meta = {
                uri: uri + '',
		url: '/' + uri.toString().replace(config.get('databasePrefix'),''),
		id: metaData.displayId,
                name: metaData.name,
                description: metaData.description || '',
                purpose: metaData.purpose || '',
                chassis: metaData.chassis || '',
                triplestore: storeUrl === stack.getDefaultStore().storeUrl ? 'public' : 'private',

                numComponents: members.length,

                components: members.map((member) => {

                    return {
			id: member.displayId,
                        name: member.name?member.name:member.displayId,
			description: member.description,
			type: member.type.replace(sbolNS,''),
                        url: '/' + member.uri.toString().replace(config.get('databasePrefix'),'')
                    }

                })

            }

	    // TODO: this only make sense when collection has a single root component
//            if(members.length > 0 && members[0].components && members[0].components.length > 0) {

//                locals.meta.displayList = getDisplayList(members[0])

//            }

            locals.sbolUrl = locals.meta.url + '/' + locals.meta.displayId + '.xml'
            locals.fastaUrl = locals.meta.url + '/' + locals.meta.displayId + '.fasta'
            locals.keywords = []
            locals.citations = []

            res.send(pug.renderFile('templates/views/collection.jade', locals))
        }
    ], function done(err) {

            res.status(500).send(err.stack)
                
    })
	
};


