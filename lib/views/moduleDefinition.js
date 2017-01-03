
var getModuleDefinition = require('../get-sbol').getModuleDefinition
var getModuleDefinitionMetadata = require('../get-sbol').getModuleDefinitionMetadata

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var prefixify = require('../prefixify')

var pug = require('pug')

var sparql = require('../sparql-collate')

var getDisplayList = require('../getDisplayList')

var wiky = require('../wiky/wiky.js');

var config = require('../config')

module.exports = function(req, res) {

    var stack = require('../stack')()

	var locals = {
        section: 'module',
        user: req.user
    }

    // todo check the prefix is real
    //req.params.prefix
    //req.params.designid

    var prefixes
    var baseUri
    var uri
    var desginId

    var meta
    var moduleDefinition

    var collections = []

    var otherModules = []
    var mappings = {}

    var stores = [
        stack.getDefaultStore()
    ]

    if(req.userStore)
        stores.push(req.userStore)

    async.series([

        function getPrefixes(next) {

            if(req.params.userId) {

		designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
		next()

	    } else {

		designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
		uri = config.get('databasePrefix') + 'public/' + designId
		next()

	    } 

        },

        function retrieveSBOL(next) {

            getModuleDefinition(null, uri, stores, function(err, sbol, _moduleDefinition) {

                if(err) {

                    next(err)

                } else {

                    moduleDefinition = _moduleDefinition

                    if(!moduleDefinition) {
                        return res.status(404).send('not found\n' + uri)
                    }
		    
                    meta = sbolmeta.summarizeModuleDefinition(moduleDefinition)
                    next()
                }
            })

        },

        function lookupCollections(next) {

            var query =
                'PREFIX sbol2: <http://sbols.org/v2#>\n' +
                'PREFIX dcterms: <http://purl.org/dc/terms/>\n' +
            'SELECT ?subject ?title WHERE {' +
                '   ?subject a sbol2:Collection .' +
                '   ?subject sbol2:member <' + uri + '> .' +
                '   ?subject dcterms:title ?title .' +
                '}'

            sparql(stores, query, (err, results) => {

                if(err) {

                    next(err)

                } else {

                    collections = results.map((result) => {
                        return {
                            uri: result.subject,
                            url: '/'+result.subject.toString().replace(config.get('databasePrefix'),''),
                            name: result.title
                        }
                    })

                    next()

                }
            })

        },

        // function getOtherModuleMetaData(next) {

        //     async.map(otherModules, (uri, next) => {

        //         getModuleDefinitionMetadata(null, uri, stores, (err, metadata) => {

        //             console.log('md for ' + uri)
        //             console.log(metadata)

        //             next(null, {
        //                 uri: uri,
        //                 name: metadata[0].name
        //             })

        //         })

        //     }, (err, moduleNameMappings) => {

        //         if(err) {

        //             next(err)

        //         } else {

        //             moduleNameMappings.forEach((mapping) => {
        //                 mappings[mapping.uri] = mapping.name
        //             })

        //             next()
                     
        //         }
        //     })

        // },

        function renderView() {

	    if (meta.description != '') {
		meta.description = wiky.process(meta.description, {})
	    }

	    meta.url = '/' + meta.uri.toString().replace(config.get('databasePrefix'),'')

            locals.meta = meta
	    locals.modules = moduleDefinition.modules
	    locals.modules.forEach((module) => {
		module.url = '/' + module.definition.uri.toString().replace(config.get('databasePrefix'),'')
	    })
	    locals.roles = moduleDefinition.roles
	    locals.models = moduleDefinition.models
	    locals.models.forEach((model) => {
		model.url = '/' + model.uri.toString().replace(config.get('databasePrefix'),'')
		model.version = model.uri.toString().substring(model.uri.toString().lastIndexOf('/')+1)
		var persId = model.uri.toString().substring(0,model.uri.toString().lastIndexOf('/'))
		model.id = persId.substring(persId.lastIndexOf('/')+1)
	    })
	    locals.functionalComponents = moduleDefinition.functionalComponents
	    locals.functionalComponents.forEach((functionalComponent) => {
		functionalComponent.link()
		functionalComponent.url = '/'  + functionalComponent.definition.uri.toString().replace(config.get('databasePrefix'),'')
		functionalComponent.typeStr = functionalComponent.access.toString().replace('http://sbols.org/v2#','') + ' '
		    + functionalComponent.direction.toString().replace('http://sbols.org/v2#','').replace('none','')
	    })

            if(req.params.userId) {
                locals.sbolUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' + moduleDefinition.displayId + '.xml'
                locals.searchUsesUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/uses'
	    } else {
                locals.sbolUrl = '/public/' + designId + '/' + moduleDefinition.displayId + '.xml'
                locals.searchUsesUrl = '/public/' + designId + '/uses'
	    } 
	    locals.annotations = moduleDefinition.getAnnotations();

            locals.keywords = []
            locals.citations = []
            locals.prefix = req.params.prefix

            locals.collections = collections

            locals.meta.description = locals.meta.description.split(';').join('<br/>')

            //locals.meta.displayList = getDisplayList(moduleDefinition)

            res.send(pug.renderFile('templates/views/moduleDefinition.jade', locals))
        }
    ], function done(err) {

            res.status(500).send(err.stack)
                
    })
	
};


