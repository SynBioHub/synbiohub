
var getModuleDefinition = require('../get-sbol').getModuleDefinition
var getModuleDefinitionMetadata = require('../get-sbol').getModuleDefinitionMetadata
var getContainingCollections = require('../get-sbol').getContainingCollections

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
        section: 'module',
        user: req.user
    }

    var meta
    var moduleDefinition

    var collections = []

    var otherModules = []
    var mappings = {}

    const { graphUris, uri, designId, share, url } = getUrisFromReq(req)

    const graphUri = graphUris[0]

    getModuleDefinition(uri, graphUris).then((result) => {

        moduleDefinition = result.object

        if(!moduleDefinition || moduleDefinition instanceof URI) {
            locals = {
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
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeModuleDefinition returned null' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

    }).then(function lookupCollections(next) {

        return getContainingCollections(uri, graphUri, req.url).then((_collections) => {
            collections = _collections
        })

    }).then(function renderView() {

        if (meta.description != '') {
            meta.description = wiky.process(meta.description, {})
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

        locals.annotations = moduleDefinition.annotations;
        locals.annotations.forEach((annotation) => {
            // var namespaces = listNamespaces(config.get("namespaces")).filter(function(namespace) {
            //     return annotation.name.indexOf(namespace.uri) === 0;
            // });
            // if(namespaces.length != 0) {
            //     var namespace = namespaces.sort((a, b) => a.uri.length - b.uri.length)[0];
            //     var prefixedName = namespace.prefix + ':' + annotation.name.slice(namespace.uri.length);
            //     annotation.name = prefixedName
            // }
            annotation.nameDef = annotation.name
            annotation.name = annotation.name.slice(annotation.name.lastIndexOf('/')+1)
            if (annotation.type === 'uri' && annotation.value.toString().startsWith(config.get('databasePrefix'))) {
                annotation.uri = '/' + annotation.value.toString().replace(config.get('databasePrefix'),'')
                annotation.value = annotation.value.substring(0,annotation.value.lastIndexOf('/'))
                annotation.value = annotation.value.slice(annotation.value.lastIndexOf('/')+1)
            } else if (annotation.type === 'uri') {
		annotation.uri = annotation.value.toString()
                annotation.value = annotation.value.slice(annotation.value.lastIndexOf('/')+1)
            }
        })
        locals.annotations = locals.annotations.filter(function(annotation) {
            return (!annotation.name.toString().startsWith('created')) && (!annotation.name.toString().startsWith('modified')) && (!annotation.name.toString().startsWith('creator')) && (!annotation.name.toString().startsWith('source')) && (!annotation.name.toString().startsWith('description')) && (!annotation.name.toString().startsWith('notes')) && (!annotation.name.toString().startsWith('status')) && (!annotation.name.toString().startsWith('partStatus')) && (!annotation.name.toString().startsWith('results')) && (!annotation.name.toString().startsWith('category')) && (!annotation.name.toString().startsWith('dominant')) && (!annotation.name.toString().startsWith('favorite')) && (!annotation.name.toString().startsWith('discontinued')) && (!annotation.name.toString().startsWith('replacedBy'))
        });

        locals.keywords = []
        locals.citations = []
        locals.prefix = req.params.prefix

        locals.collections = collections

        locals.meta.description = locals.meta.description.split(';').join('<br/>')

        //locals.meta.displayList = getDisplayList(moduleDefinition)

        res.send(pug.renderFile('templates/views/moduleDefinition.jade', locals))

    }).catch((err) => {

        locals = {
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

