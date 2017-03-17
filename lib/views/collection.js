
const { getCollectionMetaData,
        getCollectionMembers,
        getCollectionMemberCount,
        getContainingCollections
} = require('../query/collection')

var loadTemplate = require('../loadTemplate')

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var sparql = require('../sparql/sparql-collate')

var prefixify = require('../prefixify')

var pug = require('pug')

var getDisplayList = require('../getDisplayList')

var config = require('../config')

var wiky = require('../wiky/wiky.js');

var retrieveCitations = require('../citations')

var getUrisFromReq = require('../getUrisFromReq')

var sha1 = require('sha1');

var util = require('../util');

const getAttachmentsFromList = require('../attachments')

module.exports = function(req, res) {

    console.log('collection view')

	var locals = {
        config: config.get(),
        section: 'collection',
        user: req.user
    }

    var collections = []
    var submissionCitations = []
    var citations = []

    var offset = 0
    if (req.query.offset) {
	offset = req.query.offset
    }
    var limit = 50
    if (req.query.limit) {
	limit = req.query.limit
    }

    const { graphUri, uri, designId, share, url } = getUrisFromReq(req)

    var sbol
    var collection

    var mappings = {}

    var metaData
    var members

    var attachments = []

    var collectionIcon

    const collectionIcons = config.get('collectionIcons')
    
    if(collectionIcons[uri])
	collectionIcon = collectionIcons[uri]

    var templateParams = {
        uri: uri
    }

    var getCitationsQuery = loadTemplate('sparql/GetCitations.sparql', templateParams)

    var getAttachmentsQuery = loadTemplate('sparql/GetAttachments.sparql', templateParams)

    getCollectionMetaData(uri, graphUri).then((result) => {
        metaData = result
    }).then(() => {

        return Promise.all([

            getCollectionMembers(uri, graphUri, limit, offset).then((_members) => {
                members = _members
            }),

            getCollectionMemberCount(uri, graphUri).then((result) => {
                memberCount = result
            }),

            getContainingCollections(uri, graphUri).then((result) => {
                collections = result
                    /*return {
                        uri: result.subject,
                        url: '/'+result.subject.toString().replace(config.get('databasePrefix'),''),
                        name: result.title?result.title:result.displayId
                    }*/
                collections.forEach((collection) => {                    
                    if(collectionIcons[collection.uri])
                        collectionIcon = collectionIcons[collection.uri]
                })
            }),

            /*

            sparql.queryJson(getCitationsQuery, graphUri).then((results) => {

                citations = results

            }).then(() => {

                return retrieveCitations(citations).then((resolvedCitations) => {

                    submissionCitations = resolvedCitations;

                })

            }),

            sparql.queryJson(getAttachmentsQuery, graphUri).then((results) => {

                attachmentList = results

                getAttachmentsFromList.getAttachmentsFromList(graphUri, attachmentList).then((results) => { 

                    attachments = results
                })

            })*/
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

        if (metaData.mutableDescription && metaData.mutableDescription != '') {
            metaData.mutableDescriptionSource = metaData.mutableDescription
            metaData.mutableDescription = wiky.process(metaData.mutableDescription, {})
        }

        if (metaData.mutableNotes && metaData.mutableNotes != '') {
            metaData.mutableNotesSource = metaData.mutableNotes
            metaData.mutableNotes = wiky.process(metaData.mutableNotes, {})
        }

        if (metaData.mutableProvenance && metaData.mutableProvenance != '') {
            metaData.mutableProvenanceSource = metaData.mutableProvenance
            metaData.mutableProvenance = wiky.process(metaData.mutableProvenance, {})
        }

        if(req.user) {

            if(req.user.isAdmin) {

                locals.canEdit = true

            } else {

                const userUri = config.get('databasePrefix') + 'user/' + req.user.username

                if(metaData.ownedBy && metaData.ownedBy.indexOf(userUri) > -1) {

                    locals.canEdit = true

                } else {

                    locals.canEdit = false

                }

            }

        } else {

            locals.canEdit = false

        }

        locals.meta = {
            uri: uri + '',
            url: '/' + uri.toString().replace(config.get('databasePrefix'),''),
            id: metaData.displayId,
            pid: metaData.persistentIdentity,
	    wasDerivedFrom: metaData.wasDerivedFrom || '',
            version: metaData.version,
            name: metaData.name,
            description: metaData.description || '',
            creator: { description: metaData.creator || '' },
            created: { description: created || '' },
            modified: { description: modified || ''},
	    mutableDescriptionSource: metaData.mutableDescriptionSource || '',
	    mutableDescription: metaData.mutableDescription || '',
	    mutableNotesSource: metaData.mutableNotesSource || '',
	    mutableNotes: metaData.mutableNotes || '',
	    sourceSource: metaData.mutableProvenanceSource || '',
	    source: metaData.mutableProvenance || '',
	    attachments: attachments,
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
	locals.citationsSource = citations.map(function(citation) {
            return citation.citation
        }).join(',');

        res.send(pug.renderFile('templates/views/collection.jade', locals))

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

