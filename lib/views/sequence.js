
var getSequence = require('../get-sbol').getSequence
var getContainingCollections = require('../get-sbol').getContainingCollections
var filterAnnotations = require('../filterAnnotations')

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
        section: 'sequence',
        user: req.user
    }

    var meta
    var sequence

    var collections = []

    const { graphUris, uri, designId, share, url } = getUrisFromReq(req)

    var graphUri

    getSequence(uri, graphUris).then((result) => {

        sequence = result.object
        graphUri = result.object.graphUri

        if(!sequence || sequence instanceof URI) {
            locals = {
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
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeSequence returned null' ]
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
        locals.citations = []
        locals.prefix = req.params.prefix

        locals.collections = collections

        locals.meta.formatted = formatSequence(meta.elements)

	locals.meta.blastUrl = meta.type === 'AminoAcid' ?
            'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastp&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome' :
            'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastn&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome'

        locals.meta.description = locals.meta.description.split(';').join('<br/>')

        res.send(pug.renderFile('templates/views/sequence.jade', locals))

    }).catch((err) => {

        const locals = {
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


