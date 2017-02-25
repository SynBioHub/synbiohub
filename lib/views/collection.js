var getCollectionMetaData = require('../get-sbol').getCollectionMetaData

var getCollectionMembers = require('../get-sbol').getCollectionMembers

var getCollectionMemberCount = require('../get-sbol').getCollectionMemberCount

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var sparql = require('../sparql/sparql-collate')

var prefixify = require('../prefixify')

var pug = require('pug')

var getDisplayList = require('../getDisplayList')

var config = require('../config')

var retrieveCitations = require('../citations')

var getUrisFromReq = require('../getUrisFromReq')

var sha1 = require('sha1');

var util = require('../util');

var ExecutionTimer = require('../util/execution-timer')

module.exports = function(req, res) {

	var locals = {
        config: config.get(),
        section: 'collection',
        user: req.user
    }

    var collections = []
    var submissionCitations = []

    var offset = 0
    if (req.query.offset) {
	offset = req.query.offset
    }
    var limit = 50
    if (req.query.limit) {
	limit = req.query.limit
    }

    const { graphUris, uri, designId, share, url } = getUrisFromReq(req)

    var sbol
    var collection

    var mappings = {}

    var metaData
    var members
    var graphUri

    var collectionIcon

    const collectionIcons = config.get('collectionIcons')
    
    if(collectionIcons[uri])
	collectionIcon = collectionIcons[uri]

    var findOwningCollectionsQuery =
        'PREFIX sbol2: <http://sbols.org/v2#>\n' +
        'PREFIX dcterms: <http://purl.org/dc/terms/>\n' +
    'SELECT ?subject ?displayId ?title WHERE {' +
        '   ?subject a sbol2:Collection .' +
        '   ?subject sbol2:member <' + uri + '> .' +
        '   OPTIONAL { ?subject sbol2:displayId ?displayId } .' +
        '   OPTIONAL { ?subject dcterms:title ?title } .' +
        '}'
    
    var getCitationsQuery =
		'PREFIX sbol2: <http://sbols.org/v2#>\n' +
		'PREFIX purl: <http://purl.obolibrary.org/obo/>\n' +
		'SELECT\n' + 
		'    ?citation\n'+
		'WHERE {\n' +  
		'    <' + uri + '> a sbol2:Collection .\n' +
		'    <' + uri + '> purl:OBI_0001617 ?citation\n' +
		'}\n'

    getCollectionMetaData(uri, graphUris).then((result) => {
        metaData = result.metaData
        graphUri = result.graphUri
    }).then(() => {

        return Promise.all([

            getCollectionMembers(uri, graphUris, limit, offset).then((_members) => {
                members = _members
            }),

            getCollectionMemberCount(uri, graphUris).then((result) => {
		memberCount = result[0].count
	    }),

            sparql.queryJson(findOwningCollectionsQuery, graphUri).then((results) => {
                collections = results.map((result) => {
                    return {
                        uri: result.subject,
                        url: '/'+result.subject.toString().replace(config.get('databasePrefix'),''),
                        name: result.title?result.title:result.displayId
                    }
                })
		collections.forEach((collection) => {                    
                    if(collectionIcons[collection.uri])
			collectionIcon = collectionIcons[collection.uri]
		})
            }),

            sparql.queryJson(getCitationsQuery, graphUri).then((results) => {

                citations = results
                console.log(citations)

            }).then(() => {

                return retrieveCitations(citations).then((resolvedCitations) => {

                    submissionCitations = resolvedCitations;

                    console.log('got citations ' + JSON.stringify(submissionCitations));

                })

            })

        ])
        
    }).then(function renderView() {

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
        var modified = metaData.modified
        if (modified) {
            modified = modified.toString().replace('T',' ').replace('Z','').substring(0,metaData.modified.indexOf('.'))
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
            modified: modified || '',
            uploadedBy: metaData.uploadedBy || '',
            triplestore: graphUri ? 'private' : 'public',
            //triplestore: graphUri === config.get('triplestore').defaultGraph ? 'public' : 'private',

            numComponents: members.length,

            components: members.map((member) => {
                if (!member.displayId) removed++
		if (member.uri.toString().startsWith(config.get('databasePrefix'))) {
		    memberUrl = '/' + member.uri.toString().replace(config.get('databasePrefix'),'')
		    if (req.url.toString().endsWith('/share')) {
			memberUrl += '/' + sha1('synbiohub_' + sha1(member.uri) + config.get('shareLinkSalt')) + '/share'
		    }
		} else {
		    memberUrl = member.uri
		}

                return {
                    id: member.displayId,
                    name: member.name?member.name:member.displayId,
                    description: member.description,
                    type: member.type.startsWith(sbolNS)?member.type.replace(sbolNS,''):member.type.slice(member.type.lastIndexOf('/')+1),
                    typeUrl: member.type,
                    url: memberUrl
                }

            })

        }
	locals.offset = offset
	locals.limit = limit
	locals.memberCount = memberCount
	locals.firstNum = (parseInt(offset) + 1)
        locals.lastNum = (parseInt(offset) + parseInt(limit))
	if (locals.lastNum > memberCount) locals.lastNum = memberCount
        locals.previous = (locals.firstNum - (parseInt(limit)+1))
        locals.next = locals.lastNum
        if (req.originalUrl.indexOf("/?offset") !== -1) {
	    locals.originalUrl = req.originalUrl.substring(0,req.originalUrl.indexOf("/?offset"))
        } else {
	    locals.originalUrl = req.originalUrl
        }
        locals.collectionIcon = collectionIcon
	locals.numMembers = 10000
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
	locals.share = share
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

        locals.sbolUrl = url + '/' + locals.meta.id + '.xml'
        locals.fastaUrl = url + '/' + locals.meta.id + '.fasta'
        locals.keywords = []
        locals.citations = []

        var timeRender = ExecutionTimer('render collection page')
        res.send(pug.renderFile('templates/views/collection.jade', locals))
        timeRender()

    }).catch((err) => {

        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err.stack ]
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

