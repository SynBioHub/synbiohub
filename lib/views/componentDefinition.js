

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const { getComponentDefinitionMetadata } = require('../query/component-definition')
const { getContainingCollections } = require('../query/collection')

var filterAnnotations = require('../filterAnnotations')

var loadTemplate = require('../loadTemplate')

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

var striptags = require('striptags')

var URI = require('sboljs').URI

var sha1 = require('sha1');

var getUrisFromReq = require('../getUrisFromReq')

const attachments = require('../attachments')

module.exports = function(req, res) {

	var locals = {
        config: config.get(),
        section: 'component',
        user: req.user
    }

    var baseUri

    var meta
    var sbol
    var componentDefinition

    var encodedProteins = []
    var collections = []

    var otherComponents = []
    var mappings = {}

    var submissionCitations = []
    var citations = []

    var collectionIcon

    const { graphUri, uri, designId, share, url } = getUrisFromReq(req)

    var templateParams = {
        uri: uri
    }

    var getCitationsQuery = loadTemplate('sparql/GetCitations.sparql', templateParams)

    fetchSBOLObjectRecursive('ComponentDefinition', uri, graphUri).then((result) => {
        
        sbol = result.sbol
        componentDefinition = result.object

        console.log(result)

        if(!componentDefinition || componentDefinition instanceof URI) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' Record Not Found' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

        meta = sbolmeta.summarizeComponentDefinition(componentDefinition)

        if(!meta) {
            locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeComponentDefinition returned null' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

    }).then(function lookupEncodedProteins() {

        var query =
            'PREFIX sybio: <http://w3id.org/sybio/ont#>\n' +
        'SELECT ?subject WHERE {' +
            '   ?subject sybio:encodedBy <' + uri + '>' +
            '}'

        return sparql.queryJson(query, graphUri).then((results) => {

            encodedProteins = results.map((result) => {
                return result.subject
            })

        })

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

    }).then(function getOtherComponentMetaData() {

        if(meta.protein && meta.protein.encodedBy)
            otherComponents = otherComponents.concat(meta.protein.encodedBy)
                    /* todo and subcomponents */

        otherComponents = otherComponents.concat(encodedProteins)

        return Promise.all(otherComponents.map((otherComponent) => {
            
            return getComponentDefinitionMetadata(otherComponent, graphUri).then((res) => {

                mappings[otherComponent] = res.metaData.name

            })

        }))

    }).then(function renderView() {

        var isDNA = 0

        meta.triplestore = graphUri === config.get('triplestore').defaultGraph ? 'public' : 'private'

        meta.attachments = attachments.getAttachmentsFromTopLevel(sbol, componentDefinition)

        meta.types = meta.types.map((type) => {

            if (type.description && type.description.name === 'DnaRegion') isDNA = 1

            return {
                uri: type.uri,
                term: type.uri,
                description: type.description
            }

        })

        meta.roles = meta.roles.map((role) => {

            var igemPrefix = 'http://wiki.synbiohub.org/wiki/Terms/igem#partType/'

            if(!role.term && role.uri.indexOf(igemPrefix) === 0) {

                return {
                    uri: role.uri,
                    term: role.uri.slice(igemPrefix.length)
                }

            } else {

                return {
                    uri: role.uri,
                    term: role.uri,
                    description: role.description
                }

            }

        })

        if (meta.description != '') {
            meta.description = wiky.process(meta.description.toString(), {})
        }

        meta.mutableDescriptionSource = meta.mutableDescription.toString() || ''
        if (meta.mutableDescription.toString() != '') {
            meta.mutableDescription = wiky.process(meta.mutableDescription.toString(), {})
        }

        meta.mutableNotesSource = meta.mutableNotes.toString() || ''
        if (meta.mutableNotes.toString() != '') {
            meta.mutableNotes = wiky.process(meta.mutableNotes.toString(), {})
        }

        meta.sourceSource = meta.source.toString() || ''
        if (meta.source.toString() != '') {
            meta.source = wiky.process(meta.source.toString(), {})
        }

        if (meta.isReplacedBy.uri != '') {
            meta.isReplacedBy.uri = '/' + meta.isReplacedBy.uri.toString().replace(config.get('databasePrefix'),'')
            meta.isReplacedBy.id = meta.isReplacedBy.uri.toString().replace('/public/','').replace('/1','') + ' '
            meta.isReplacedBy.id = meta.isReplacedBy.id.substring(meta.isReplacedBy.id.indexOf('/')+1)
        }

        meta.url = '/' + meta.uri.toString().replace(config.get('databasePrefix'),'')

        locals.meta = meta

        locals.components = componentDefinition.components
        locals.components.forEach((component) => {
            component.link()
            if (component.definition.uri) {
                if (component.definition.uri.toString().startsWith(config.get('databasePrefix'))) {
                    component.url = '/'  + component.definition.uri.toString().replace(config.get('databasePrefix'),'')
                } else {
                    component.url = component.definition.uri
                }
            } else {
                component.url = component.definition.toString()
            }
            component.typeStr = component.access.toString().replace('http://sbols.org/v2#','')
        })
        locals.meta.sequences.forEach((sequence) => {
            if (sequence.uri.toString().startsWith(config.get('databasePrefix'))) {
                sequence.url = '/'  + sequence.uri.toString().replace(config.get('databasePrefix'),'')
                if (req.url.toString().endsWith('/share')) {
                    sequence.url += '/' + sha1('synbiohub_' + sha1(sequence.uri) + config.get('shareLinkSalt')) + '/share'
                }            
            } else {
                sequence.url = sequence.uri
            }
        })


        locals.sbolUrl = url + '/' + meta.id + '.xml'
        locals.fastaUrl = url + '/' + meta.id + '.fasta'
        locals.genBankUrl = url + '/' + meta.id + '.gb'
        if(req.params.userId) {
            locals.searchUsesUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/uses'
            locals.searchTwinsUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/twins'
            locals.dataIntegrationUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/integrate'
        } else {
            locals.searchUsesUrl = '/public/' + designId + '/uses'
            locals.searchTwinsUrl = '/public/' + designId + '/twins'
            locals.dataIntegrationUrl = '/public/' + designId + '/integrate'
        } 

        locals.keywords = []
        locals.prefix = req.params.prefix

        locals.encodedProteins = encodedProteins.map((uri) => {

            return {
                uri: uri,
                name: mappings[uri],
                url: uri
            }

        })
        console.log(JSON.stringify(locals.encodedProteins))

        locals.collections = collections

        // locals.meta.sequences.forEach((sequence) => {

        //     sequence.formatted = formatSequence(sequence.elements)

        //     sequence.blastUrl = sequence.type === 'AminoAcid' ?
        //         'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastp&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome' :
        //         'http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastn&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome'

        // })

        locals.meta.description = locals.meta.description.split(';').join('<br/>')
        locals.metaDesc = striptags(locals.meta.description).trim()
        locals.title = locals.meta.name + ' - SynBioHub'
        locals.collectionIcon = collectionIcon
        locals.submissionCitations = submissionCitations
        locals.citationsSource = citations.map(function(citation) {
            return citation.citation
        }).join(',');

        if(locals.meta.protein) {

            if(locals.meta.protein.encodedBy) {

                locals.meta.protein.encodedBy = locals.meta.protein.encodedBy.map((uri) => {

                    var prefixified = prefixify(uri, prefixes)

                    return {
                        uri: uri,
                        name: mappings[uri],
                        url: '/entry/' + prefixified.prefix + '/' + prefixified.uri
                    }

                })

            }
        }

        if (isDNA) {
            locals.meta.displayList = getDisplayList(componentDefinition)
            if (req.url.toString().endsWith('/share')) {
                console.log(locals.meta.displayList.components)
            }
        }

        if(req.user) {

            if(req.user.isAdmin) {

                locals.canEdit = true

            } else {

                const ownedBy = componentDefinition.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')
                const userUri = config.get('databasePrefix') + 'user/' + req.user.username

                if(ownedBy && ownedBy.indexOf(userUri) > -1) {

                    locals.canEdit = true

                } else {

                    locals.canEdit = false

                }

            }

        } else {

            locals.canEdit = false

        }

        locals.annotations = filterAnnotations(componentDefinition.annotations);
        console.log('locals:'+locals.submissionCitations)

        res.send(pug.renderFile('templates/views/componentDefinition.jade', locals))

    }).catch((err) => {

        const locals = {
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



