var getCollectionMetaData = require('../get-sbol').getCollectionMetaData

var getCollectionMembers = require('../get-sbol').getCollectionMembers

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var sparql = require('../sparql/sparql-collate')

var prefixify = require('../prefixify')

var pug = require('pug')

var getDisplayList = require('../getDisplayList')

var config = require('../config')

var retrieveCitations = require('../citations')

module.exports = function(req, res) {

    var stack = require('../stack')()

	var locals = {
        section: 'collection',
        user: req.user
    }

    var designId
    var uri
    var collections = []
    var submissionCitations = []

    if(req.params.userId) {
	designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
    } else {
	designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	uri = config.get('databasePrefix') + 'public/' + designId
    } 

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
		           
		    locals = {
			section: 'errors',
			user: req.user,
			errors: [ err ]
		    }
		    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
		    return       

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
		           
		    locals = {
			section: 'errors',
			user: req.user,
			errors: [ err ]
		    }
		    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
		    return       

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

        function getCitations(next) {

	    console.log('get citations')

            var query =
		'PREFIX sbol2: <http://sbols.org/v2#>\n' +
		'PREFIX purl: <http://purl.obolibrary.org/obo/>\n' +
		'SELECT\n' + 
		'    ?citation\n'+
		'WHERE {\n' +  
		'    <' + uri + '> a sbol2:Collection .\n' +
		'    <' + uri + '> purl:OBI_0001617 ?citation\n' +
		'}\n'

            sparql(stores, query, (err, results) => {

                if(err) {

                    next(err)

                } else {

                    citations = results
		    console.log(citations)
                    next()

                }
            })

        },

        function lookupCitations(next) {

            console.log('lookupCitations');
	    
            retrieveCitations(citations, function(err, citations) {

                submissionCitations = citations;

                console.log('got citations ' + JSON.stringify(submissionCitations));

		next();

            });
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

	    var removed = 0
	    var created = metaData.created
	    if (created) {
		created = created.toString().replace('T',' ').replace('Z','').substring(0,metaData.created.indexOf('.'))
	    }

            locals.meta = {
                uri: uri + '',
		url: '/' + uri.toString().replace(config.get('databasePrefix'),''),
		id: metaData.displayId,
		pid: metaData.persistentIdentity,
		version: metaData.version,
                name: metaData.name,
                description: metaData.description || '',
                creator: metaData.creator || '',
                created: created || '',
                uploadedBy: metaData.uploadedBy || '',
                triplestore: storeUrl === stack.getDefaultStore().storeUrl ? 'public' : 'private',

                numComponents: members.length,

                components: members.map((member) => {
		    
		    
		    // if (member.displayId) {
		    // 	memberId = member.displayId
		    // } else {
		    // 	if(req.params.userId) {
		    // 	    memberId = member.uri.toString().replace(config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + req.params.collectionId + '/','').replace('/'+req.params.version,'')
		    // 	} else {
		    // 	    memberId = member.uri.toString().replace(config.get('databasePrefix') + 'public/' + req.params.collectionId + '/','').replace('/'+req.params.version,'')
		    // 	} 
		    // }
		    if (!member.displayId) removed++

                    return {
			id: member.displayId,
                        name: member.name?member.name:member.displayId,
			description: member.description,
			type: member.type.startsWith(sbolNS)?member.type.replace(sbolNS,''):member.type.slice(member.type.lastIndexOf('/')+1),
			typeUrl: member.type,
                        url: member.uri.toString().startsWith(config.get('databasePrefix'))?'/' + member.uri.toString().replace(config.get('databasePrefix'),''):member.uri
                    }

                })

            }
	    locals.meta.numComponents = locals.meta.numComponents - removed
	    locals.meta.components.forEach((annotation) => {
	    	var namespaces = listNamespaces(config.get("namespaces")).filter(function(namespace) {
	    	    return annotation.type.indexOf(namespace.uri) === 0;
	    	});
	    	if(namespaces.length != 0) {
	    	    var namespace = namespaces.sort((a, b) => a.uri.length - b.uri.length)[0];
	    	    var prefixedName = namespace.prefix + ':' + annotation.type.slice(namespace.uri.length);
	    	    annotation.type = prefixedName
	    	}
	    })
	    locals.submissionCitations = submissionCitations
	    console.log('LOCALS')
	    console.log(locals.submissionCitations)

	    // TODO: need to fetch annotations
	    // locals.annotations = model.annotations;
	    // locals.annotations.forEach((annotation) => {
	    // 	var namespaces = listNamespaces(config.get("namespaces")).filter(function(namespace) {
	    // 	    return annotation.name.indexOf(namespace.uri) === 0;
	    // 	});
	    // 	if(namespaces.length != 0) {
	    // 	    var namespace = namespaces.sort((a, b) => a.uri.length - b.uri.length)[0];
	    // 	    var prefixedName = namespace.prefix + ':' + annotation.name.slice(namespace.uri.length);
	    // 	    annotation.name = prefixedName
	    // 	}
	    // 	if (annotation.type === 'uri' && annotation.value.toString().startsWith(config.get('databasePrefix'))) {
	    // 	    annotation.value = '/' + annotation.value.toString().replace(config.get('databasePrefix'),'')
	    // 	}
	    // })
	    //locals.annotations = locals.annotations.filter(function(annotation) {
//		return (!annotation.name.toString().startsWith('dcterms'))&&(!annotation.name.toString().startsWith('igem'))
	    //});

	    // TODO: this only make sense when collection has a single root component
//            if(members.length > 0 && members[0].components && members[0].components.length > 0) {

//                locals.meta.displayList = getDisplayList(members[0])

//            }

            locals.sbolUrl = locals.meta.url + '/' + locals.meta.id + '.xml'
            locals.fastaUrl = locals.meta.url + '/' + locals.meta.id + '.fasta'
            locals.keywords = []
            locals.citations = []

            res.send(pug.renderFile('templates/views/collection.jade', locals))
        }
    ], function done(err) {

        locals = {
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
        return        
                
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

