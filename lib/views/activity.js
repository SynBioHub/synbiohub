
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const { getContainingCollections } = require('../query/local/collection')

var filterAnnotations = require('../filterAnnotations')
var retrieveCitations = require('../citations')

const shareImages = require('../shareImages')

var loadTemplate = require('../loadTemplate')

var sbolmeta = require('sbolmeta')

var async = require('async')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var wiky = require('../wiky/wiky.js');

var config = require('../config')

var URI = require('sboljs').URI

var getUrisFromReq = require('../getUrisFromReq')

const attachments = require('../attachments')

const uriToUrl = require('../uriToUrl')

var sha1 = require('sha1');

module.exports = function(req, res) {

	var locals = {
        config: config.get(),
        section: 'component',
        user: req.user
    }

    var meta
    var sbol
    var activity
    var collectionIcon 
    var remote

    var collections = []

    var submissionCitations = []
    var citations = []

    const { graphUri, uri, designId, share, url } = getUrisFromReq(req, res)

    var templateParams = {
        uri: uri
    }

    var getCitationsQuery = loadTemplate('sparql/GetCitations.sparql', templateParams)
 
    fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
	
        sbol = result.sbol
        activity = result.object
        remote = result.remote

        if(!activity || activity instanceof URI) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' Record Not Found: ' + activity ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return Promise.reject()
        }
        meta = sbolmeta.summarizeGenericTopLevel(activity)
        if(!meta) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeGenericTopLevel returned null' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return Promise.reject()
        }

    }).then(function lookupCollections() {

        return Promise.all([
	    getContainingCollections(uri, graphUri, req.url).then((_collections) => {

		collections = _collections

		collections.forEach((collection) => {

		    collection.url = uriToUrl(collection.uri)

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

                    //console.log('got citations ' + JSON.stringify(submissionCitations));

                })

            })

        ])

    }).then(function renderView() {

	if (meta.description != '') {
	    meta.description = wiky.process(meta.description, {})
	}

        meta.mutableDescriptionSource = meta.mutableDescription.toString() || ''
        if (meta.mutableDescription.toString() != '') {
	    meta.mutableDescription = shareImages(req,meta.mutableDescription.toString())
            meta.mutableDescription = wiky.process(meta.mutableDescription.toString(), {})
        }

        meta.mutableNotesSource = meta.mutableNotes.toString() || ''
        if (meta.mutableNotes.toString() != '') {
	    meta.mutableNotes = shareImages(req,meta.mutableNotes.toString())
            meta.mutableNotes = wiky.process(meta.mutableNotes.toString(), {})
        }

        meta.sourceSource = meta.source.toString() || ''
        if (meta.source.toString() != '') {
	    meta.source = shareImages(req,meta.source.toString())
            meta.source = wiky.process(meta.source.toString(), {})
        }

        meta.attachments = attachments.getAttachmentsFromTopLevel(sbol, activity, 
							  req.url.toString().endsWith('/share'))

        locals.canEdit = false

        if(!remote && req.user) {

            const ownedBy = activity.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')
            const userUri = config.get('databasePrefix') + 'user/' + req.user.username

            if(ownedBy && ownedBy.indexOf(userUri) > -1) {

                locals.canEdit = true
		
            } 

        }

	meta.url = '/' + meta.uri.toString().replace(config.get('databasePrefix'),'')
	if (req.url.toString().endsWith('/share')) {
	    meta.url += '/' + sha1('synbiohub_' + sha1(meta.uri) + config.get('shareLinkSalt')) + '/share'
	}

	if (activity.wasGeneratedBy) {
	    meta.wasGeneratedBy = { uri: activity.wasGeneratedBy.uri?activity.wasGeneratedBy.uri:activity.wasGeneratedBy,
				    url: uriToUrl(activity.wasGeneratedBy,req)
				  }
	}

        locals.meta = meta
        
        locals.meta.triplestore = graphUri ? 'private' : 'public'
        locals.meta.remote = remote

	locals.rdfType = {
	    name : 'Activity',
	    url : 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Activity'
	}
	
        locals.usages = activity.usages
        locals.usages.forEach((usage) => {
            usage.link()
            if (usage.entity.uri) {
                usage.defId = usage.entity.displayId
                usage.defName = usage.entity.name
                if (usage.entity.uri.toString().startsWith(config.get('databasePrefix'))) {
                    usage.url = '/' + usage.entity.uri.toString().replace(config.get('databasePrefix'),'')
		    if (usage.entity.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
			usage.url += '/' + sha1('synbiohub_' + sha1(usage.entity.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		    }  
                } else { 
                    usage.url = usage.entity.uri.toString()
		}
            } else {
                usage.defId = usage.entity.toString()
                usage.defName = ''
                if (usage.entity.toString().startsWith(config.get('databasePrefix'))) {
                    usage.url = '/' + usage.entity.toString().replace(config.get('databasePrefix'),'')
		    if (usage.entity.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
			usage.url += '/' + sha1('synbiohub_' + sha1(usage.entity.toString()) + config.get('shareLinkSalt')) + '/share'
		    }  
                } else { 
                    usage.url = usage.entity.toString()
		}
            }
	    usage.typeStr = ''
	    usage.roles.forEach((role) => {
		usage.typeStr += role.toString().replace('http://sbols.org/v2#','') + ' '
	    })
        })

	locals.associations = activity.associations
        locals.associations.forEach((association) => {
            association.link()
            if (association.agent.uri) {
                association.agent.defId = association.agent.displayId
                association.agent.defName = association.agent.name
                if (association.agent.uri.toString().startsWith(config.get('databasePrefix'))) {
                    association.agent.url = '/' + association.agent.uri.toString().replace(config.get('databasePrefix'),'')
		    if (association.agent.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
			association.agent.url += '/' + sha1('synbiohub_' + sha1(association.agent.uri.toString()) + config.get('shareLinkSalt')) + '/share'
		    }  
                } else { 
                    association.agent.url = association.agent.uri.toString()
		}
            } else {
                association.agent.defId = association.agent.toString()
                association.agent.defName = ''
                if (association.agent.toString().startsWith(config.get('databasePrefix'))) {
                    association.agent.url = '/' + association.agent.toString().replace(config.get('databasePrefix'),'')
		    if (association.agent.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
			association.agent.url += '/' + sha1('synbiohub_' + sha1(association.agent.toString()) + config.get('shareLinkSalt')) + '/share'
		    }  
                } else { 
                    association.agent.url = association.agent.toString()
		}
            }
	    association.typeStr = ''
	    association.roles.forEach((role) => {
		association.typeStr += role.toString().replace('http://sbols.org/v2#','') + ' '
	    })
	    if (association.plan) {
		if (association.plan.uri) {
                    association.plan.defId = association.plan.displayId
                    association.plan.defName = association.plan.name
                    if (association.plan.uri.toString().startsWith(config.get('databasePrefix'))) {
			association.plan.url = '/' + association.plan.uri.toString().replace(config.get('databasePrefix'),'')
			if (association.plan.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
			    association.plan.url += '/' + sha1('synbiohub_' + sha1(association.plan.uri.toString()) + config.get('shareLinkSalt')) + '/share'
			}  
                    } else { 
			association.plan.url = association.plan.uri.toString()
		    }
		} else {
                    association.plan.defId = association.plan.toString()
                    association.plan.defName = ''
                    if (association.plan.toString().startsWith(config.get('databasePrefix'))) {
			association.plan.url = '/' + association.plan.toString().replace(config.get('databasePrefix'),'')
			if (association.plan.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
			    association.plan.url += '/' + sha1('synbiohub_' + sha1(association.plan.toString()) + config.get('shareLinkSalt')) + '/share'
			}  
                    } else { 
			association.plan.url = association.plan.toString()
		    }
		}
	    }
        })


        locals.annotations = filterAnnotations(req,activity.annotations);

	locals.share = share
        locals.sbolUrl = url + '/' + meta.id + '.xml'
        if(req.params.userId) {
            locals.searchUsesUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/uses'
	    locals.makePublic = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/makePublic'
            locals.remove = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/remove'
	} else {
            locals.searchUsesUrl = '/public/' + designId + '/uses'
            locals.remove = '/public/' + designId + '/remove'
	} 

        locals.keywords = []
        locals.citations = []
        locals.prefix = req.params.prefix

        locals.collections = collections

        locals.collectionIcon = collectionIcon

        locals.submissionCitations = submissionCitations
	locals.citationsSource = citations.map(function(citation) {
            return citation.citation
        }).join(',');

        locals.meta.description = locals.meta.description.split(';').join('<br/>')

        res.send(pug.renderFile('templates/views/activity.jade', locals))

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



