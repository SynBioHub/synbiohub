
var getModuleDefinition = require('../get-sbol').getModuleDefinition
var getModuleDefinitionMetadata = require('../get-sbol').getModuleDefinitionMetadata
var getContainingCollections = require('../get-sbol').getContainingCollections
var filterAnnotations = require('../filterAnnotations')
var retrieveCitations = require('../citations')

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var prefixify = require('../prefixify')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var getDisplayList = require('../getDisplayList')

var wiky = require('../wiky/wiky.js');

var config = require('../config')

var URI = require('urijs')

var sha1 = require('sha1');

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	var locals = {
        config: config.get(),
        section: 'module',
        user: req.user
    }

    var meta
    var moduleDefinition
    var collectionIcon

    var collections = []

    var submissionCitations = []

    var otherModules = []
    var mappings = {}

    const { graphUris, uri, designId, share, url } = getUrisFromReq(req)

    const graphUri = graphUris[0]
    
    var getCitationsQuery =
		'PREFIX sbol2: <http://sbols.org/v2#>\n' +
		'PREFIX purl: <http://purl.obolibrary.org/obo/>\n' +
		'SELECT\n' + 
		'    ?citation\n'+
		'WHERE {\n' +  
		'    <' + uri + '> purl:OBI_0001617 ?citation\n' +
		'}\n'

    getModuleDefinition(uri, graphUris).then((result) => {

        moduleDefinition = result.object

        if(!moduleDefinition || moduleDefinition instanceof URI) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' Record Not Found' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

        meta = sbolmeta.summarizeModuleDefinition(moduleDefinition)
        if(!meta) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeModuleDefinition returned null' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

    }).then(function lookupCollections() {

        return Promise.all([
	    getContainingCollections(uri, graphUri, req.url).then((_collections) => {

		collections = _collections

		collections.forEach((collection) => {

                    const collectionIcons = config.get('collectionIcons')
                    
                    if(collectionIcons[collection.uri])
			collectionIcon = collectionIcons[collection.uri]
		})
            }),

	    sparql.queryJson(getCitationsQuery, graphUri).then((results) => {

                citations = results

            }).then(() => {

                return retrieveCitations(citations).then((resolvedCitations) => {

                    submissionCitations = resolvedCitations;

                    console.log('got citations ' + JSON.stringify(submissionCitations));

                })

            })

        ])

    }).then(function renderView() {

        if (meta.description != '') {
            meta.description = wiky.process(meta.description, {})
        }

        if (meta.mutableDescription != '') {
            meta.mutableDescriptionSource = meta.mutableDescription
            meta.mutableDescription = wiky.process(meta.mutableDescription, {})
        }

        if (meta.mutableNotes != '') {
            meta.mutableNotesSource = meta.mutableNotes
            meta.mutableNotes = wiky.process(meta.mutableNotes, {})
        }

        if (meta.source != '') {
            meta.sourceSource = meta.source
            meta.source = wiky.process(meta.source, {})
        }

        meta.url = '/' + meta.uri.toString().replace(config.get('databasePrefix'),'')

        locals.meta = meta
        locals.modules = moduleDefinition.modules
        locals.modules.forEach((module) => {
            if (module.definition.uri) {
                module.defId = module.definition.displayId
                module.defName = module.definition.name
		console.log(module.definition.uri.toString())
		console.log(config.get('databasePrefix'))
                if (module.definition.uri.toString().startsWith(config.get('databasePrefix'))) {
                    module.url = '/' + module.definition.uri.toString().replace(config.get('databasePrefix'),'')
		    if (req.url.toString().endsWith('/share')) {
			module.url += '/' + sha1('synbiohub_' + sha1(module.definition.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		    }            
                } else {
                    module.url = module.definition.uri.toString()
		}
            } else {
                module.defId = module.definition.toString()
                module.defName = ''
                module.url = module.definition.toString()
            }
        })
        locals.roles = moduleDefinition.roles
        locals.models = moduleDefinition.models
        locals.models.forEach((model) => {
            if (model.uri) {
                if (model.uri.toString().startsWith(config.get('databasePrefix'))) {
                    model.url = '/' + model.uri.toString().replace(config.get('databasePrefix'),'')
		    if (req.url.toString().endsWith('/share')) {
			model.url += '/' + sha1('synbiohub_' + sha1(model.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		    }            
                } else {
                    model.url = model.uri.toString()
                }
                model.version = model.uri.toString().substring(model.uri.toString().lastIndexOf('/')+1)
                var persId = model.uri.toString().substring(0,model.uri.toString().lastIndexOf('/'))
                model.id = persId.substring(persId.lastIndexOf('/')+1)
            } else {
                model.url = model.toString()
                model.id = model.toString()
                model.name = ''
            }
        })
        locals.functionalComponents = moduleDefinition.functionalComponents
        locals.functionalComponents.forEach((functionalComponent) => {
            functionalComponent.link()
            if (functionalComponent.definition.uri) {
                functionalComponent.defId = functionalComponent.definition.displayId
                functionalComponent.defName= functionalComponent.definition.name
                if (functionalComponent.definition.uri.toString().startsWith(config.get('databasePrefix'))) {
                    functionalComponent.url = '/' + functionalComponent.definition.uri.toString().replace(config.get('databasePrefix'),'')
		    if (req.url.toString().endsWith('/share')) {
			functionalComponent.url += '/' + sha1('synbiohub_' + sha1(functionalComponent.definition.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		    }            
                } else { 
                    functionalComponent.url = functionalComponent.uri.toString()
		}
            } else {
                functionalComponent.defId = functionalComponent.definition.toString()
                functionalComponent.defName = ''
                functionalComponent.url = functionalComponent.definition.toString()
            }
            functionalComponent.typeStr = functionalComponent.access.toString().replace('http://sbols.org/v2#','') + ' '
                + functionalComponent.direction.toString().replace('http://sbols.org/v2#','').replace('none','')
        })

        locals.sbolUrl = url + '/' + moduleDefinition.displayId + '.xml'
        if(req.params.userId) {
            locals.searchUsesUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/uses'
        } else {
            locals.searchUsesUrl = '/public/' + designId + '/uses'
        } 

        if(req.user) {

            if(req.user.isAdmin) {

                locals.canEdit = true

            } else {

                const ownedBy = moduleDefinition.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')
                const userUri = config.get('databasePrefix') + 'user/' + req.user.username

                if(ownedBy.indexOf(userUri) > -1) {

                    locals.canEdit = true

                } else {

                    locals.canEdit = false

                }

            }

        } else {

            locals.canEdit = false

        }

        locals.annotations = filterAnnotations(moduleDefinition.annotations);

        locals.keywords = []
        locals.citations = []
        locals.prefix = req.params.prefix
        locals.collectionIcon = collectionIcon

        locals.submissionCitations = submissionCitations

        locals.collections = collections

        locals.meta.description = locals.meta.description.split(';').join('<br/>')

        //locals.meta.displayList = getDisplayList(moduleDefinition)

        res.send(pug.renderFile('templates/views/moduleDefinition.jade', locals))

    }).catch((err) => {

        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    })
	
};

function listNamespaces(xmlAttribs) {

    var namespaces = [];

    Object.keys(xmlAttribs).forEach(function(attrib) {

        var tokens = attrib.split(':');

        if(tokens[0] === 'xmlns') {

            namespaces.push({
                prefix: tokens[1],
                uri: xmlAttribs[attrib]
            })
        }
    });

    return namespaces;
}

