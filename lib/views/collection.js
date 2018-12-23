
const { getCollectionMetaData,
    getCollectionMembers,
    getCollectionMemberCount,
    getContainingCollections
} = require('../query/collection')

const getOwnedBy = require('../query/ownedBy')

var filterAnnotations = require('../filterAnnotations')

var loadTemplate = require('../loadTemplate')

var formatSequence = require('sequence-formatter')

var async = require('async')

var sparql = require('../sparql/sparql-collate')

var prefixify = require('../prefixify')

var pug = require('pug')

var getDisplayList = require('visbol/lib/getDisplayList')

var config = require('../config')

var wiky = require('../wiky/wiky.js');

var retrieveCitations = require('../citations')

var getUrisFromReq = require('../getUrisFromReq')

var sha1 = require('sha1');

var util = require('../util');

var generateDataset = require('../bioschemas/Dataset')

const uriToUrl = require('../uriToUrl')

const getAttachmentsFromList = require('../attachments')

const shareImages = require('../shareImages')

var extend = require('xtend')

module.exports = function (req, res) {

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
    var limit = config.get('defaultLimit')
    if (req.query.limit) {
        limit = req.query.limit
    }

    const { graphUri, uri, designId, share, url } = getUrisFromReq(req, res)

    var sbol
    var collection

    var mappings = {}

    var members = []

    var memberCount = 0

    var attachments = []

    var collectionIcon

    const collectionIcons = config.get('collectionIcons')

    if (collectionIcons[uri])
        collectionIcon = collectionIcons[uri]

    var templateParams = {
        uri: uri
    }

    var getCitationsQuery = loadTemplate('sparql/GetCitations.sparql', templateParams)

    var getAttachmentsQuery = loadTemplate('sparql/GetAttachments.sparql', templateParams)

    getCollectionMetaData(uri, graphUri).then((_metaData) => {

        metaData = _metaData

        return Promise.all([

            getContainingCollections(uri, graphUri).then((result) => {
                collections = result
                collections.forEach((collection) => {
                    collection.url = uriToUrl(collection.uri)
                    if (collectionIcons[collection.uri])
                        collectionIcon = collectionIcons[collection.uri]
                })
            }),

            sparql.queryJson(getCitationsQuery, graphUri).then((results) => {

                citations = results

            }).then(() => {

                return retrieveCitations(citations).then((resolvedCitations) => {

                    submissionCitations = resolvedCitations;

                })

            }),

            sparql.queryJson(getAttachmentsQuery, graphUri).then((results) => {

                attachmentList = results

                return getAttachmentsFromList.getAttachmentsFromList(graphUri, attachmentList,
								     req.url.toString().endsWith('/share')).then((results) => {

                    attachments = results

                })

            })
        ])

    }).then(function renderView() {

        locals.collections = collections

        var removed = 0
        var created = metaData.created
        if (created) {
            created = created.toString().replace('T', ' ').replace('Z', '')
            if (created.indexOf('.') > 0) {
                created = created.substring(0, metaData.created.indexOf('.'))
            }
        }
        var modified = metaData.modified
        if (modified) {
            modified = modified.toString().replace('T', ' ').replace('Z', '')
            if (modified.indexOf('.') > 0) {
                modified = modified.substring(0, metaData.modified.indexOf('.'))
            }
        }

        if (metaData.mutableDescription && metaData.mutableDescription != '') {
	    metaData.mutableDescription = shareImages(req,metaData.mutableDescription)
            metaData.mutableDescriptionSource = metaData.mutableDescription
            metaData.mutableDescription = wiky.process(metaData.mutableDescription, {})
        }

        if (metaData.mutableNotes && metaData.mutableNotes != '') {
	    metaData.mutableNotes = shareImages(req,metaData.mutableNotes)
            metaData.mutableNotesSource = metaData.mutableNotes
            metaData.mutableNotes = wiky.process(metaData.mutableNotes, {})
        }

        if (metaData.mutableProvenance && metaData.mutableProvenance != '') {
	    metaData.mutableProvenance = shareImages(req,metaData.mutableProvenance)
            metaData.mutableProvenanceSource = metaData.mutableProvenance
            metaData.mutableProvenance = wiky.process(metaData.mutableProvenance, {})
        }

        locals.bioschemas = generateDataset(extend(metaData, { uri, colUrl:uri}))

        locals.meta = {
            uri: uri + '',
            url: uriToUrl(uri + ''),
            id: metaData.displayId,
            pid: metaData.persistentIdentity,
            wasDerivedFrom: metaData.wasDerivedFrom || '',
            wasDerivedFroms: metaData.wasDerivedFrom?
		[ { uri: metaData.wasDerivedFrom,
		    url: metaData.wasDerivedFrom } ]:[],
            wasGeneratedBy: { uri: metaData.wasGeneratedBy || '',
			      url: uriToUrl(metaData.wasGeneratedBy,req)
			    },
            wasGeneratedBys: metaData.wasGeneratedBy?
		[ { uri: metaData.wasGeneratedBy || '',
		    url: uriToUrl(metaData.wasGeneratedBy,req)
		  } ]:[],
            version: metaData.version,
            name: metaData.name,
            description: metaData.description || '',
            creator: { description: metaData.creator || '' },
            created: { description: created || '' },
            modified: { description: modified || '' },
            mutableDescriptionSource: metaData.mutableDescriptionSource || '',
            mutableDescription: metaData.mutableDescription || '',
            mutableNotesSource: metaData.mutableNotesSource || '',
            mutableNotes: metaData.mutableNotes || '',
            sourceSource: metaData.mutableProvenanceSource || '',
            source: metaData.mutableProvenance || '',
            attachments: attachments,
            triplestore: graphUri ? 'private' : 'public',
            graphUri: graphUri,
	    memberCount: 0
        }

	if (req.url.toString().endsWith('/share')) {
	    locals.meta.url += '/' + sha1('synbiohub_' + sha1(locals.meta.uri) + config.get('shareLinkSalt')) + '/share'
	}

	locals.rdfType = {
	    name : 'Collection',
	    url : 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Collection'
	}
        if (req.originalUrl.indexOf("/?offset") !== -1) {
            locals.originalUrl = req.originalUrl.substring(0, req.originalUrl.indexOf("/?offset"))
        } else {
            locals.originalUrl = req.originalUrl
        }
        locals.collectionIcon = collectionIcon
        locals.share = share
        locals.submissionCitations = submissionCitations
        locals.meta.remote = metaData.remote

        locals.citationsSource = citations.map(function (citation) {
            return citation.citation
        }).join(',');

        locals.title = metaData.name + ' ‒ ' + config.get('instanceName')

        if (metaData.description) {
            locals.metaDesc = metaData.description
        } else if (metaData.mutableDescription) {
            locals.metaDesc = metaData.mutableDescription
        } else {
            locals.metaDesc = 'Collection containing ' + members.length + ' member(s)'
        }

        if (metaData.wasDerivedFrom) {
            locals.metaDesc += '.  Derived from ' + metaData.wasDerivedFrom
        }

	locals.meta.canEdit = false

	getOwnedBy(uri, graphUri).then((ownedBy) => {

            if(!metaData.remote && req.user) {

		const userUri = config.get('databasePrefix') + 'user/' + req.user.username

		if(ownedBy && ownedBy.indexOf(userUri) > -1) {

                    locals.meta.canEdit = true

		}

            }

	    var annotations = ownedBy.map(owner => {
		return { name : 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy',
			 type : 'uri',
			 value : owner }
	    })

            locals.annotations = filterAnnotations(req,annotations);

            res.send(pug.renderFile('templates/views/collection.jade', locals))
	})

    }).catch((err) => {

        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [err.stack ? err.stack : err]
        }


        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })

};
