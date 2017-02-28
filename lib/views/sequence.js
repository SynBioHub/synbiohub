
var getSequence = require('../get-sbol').getSequence
var getContainingCollections = require('../get-sbol').getContainingCollections
var filterAnnotations = require('../filterAnnotations')
var retrieveCitations = require('../citations')

var sbolmeta = require('sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var wiky = require('../wiky/wiky.js');

var config = require('../config')

var URI = require('urijs')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	var locals = {
        config: config.get(),
        section: 'sequence',
        user: req.user
    }

    var meta
    var sequence
    var collectionIcon 

    var collections = []

    var submissionCitations = []

    const { graphUris, uri, designId, share, url } = getUrisFromReq(req)

    var graphUri
    
    var getCitationsQuery =
		'PREFIX sbol2: <http://sbols.org/v2#>\n' +
		'PREFIX purl: <http://purl.obolibrary.org/obo/>\n' +
		'SELECT\n' + 
		'    ?citation\n'+
		'WHERE {\n' +  
		'    <' + uri + '> purl:OBI_0001617 ?citation\n' +
		'}\n'

    getSequence(uri, graphUris).then((result) => {

        sequence = result.object
        graphUri = result.object.graphUri

        if(!sequence || sequence instanceof URI) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' Record Not Found' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

        meta = sbolmeta.summarizeSequence(sequence)
        if(!meta) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeSequence returned null' ]
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

	meta.encoding = sequence.encoding

        locals.meta = meta
	
        if(req.user) {

            if(req.user.isAdmin) {

                locals.canEdit = true

            } else {

                const ownedBy = sequence.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')
                const userUri = config.get('databasePrefix') + 'user/' + req.user.username

                if(owned && ownedBy.indexOf(userUri) > -1) {

                    locals.canEdit = true

                } else {

                    locals.canEdit = false

                }

            }

        } else {

            locals.canEdit = false

        }

        locals.annotations = filterAnnotations(sequence.annotations);

        locals.sbolUrl = url + '/' + meta.id + '.xml'
        locals.fastaUrl = url + '/' + meta.id + '.fasta'
        if(req.params.userId) {
            locals.searchUsesUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/uses'
            locals.searchTwinsUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/twins'
	} else {
            locals.searchUsesUrl = '/public/' + designId + '/uses'
            locals.searchTwinsUrl = '/public/' + designId + '/twins'
	} 

        locals.keywords = []
        locals.prefix = req.params.prefix

        locals.collections = collections

        locals.collectionIcon = collectionIcon

        locals.submissionCitations = submissionCitations

        locals.meta.formatted = formatSequence(meta.elements)

	locals.meta.blastUrl = meta.type === 'AminoAcid' ?
            'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastp&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome' :
            'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastn&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome'

        locals.meta.description = locals.meta.description.split(';').join('<br/>')

        res.send(pug.renderFile('templates/views/sequence.jade', locals))

    }).catch((err) => {

        const locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }

        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })
	
	}

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


