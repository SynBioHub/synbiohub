
var getComponentDefinition = require('../get-sbol').getComponentDefinition
var getComponentDefinitionMetadata = require('../get-sbol').getComponentDefinitionMetadata
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

var striptags = require('striptags')

var URI = require('urijs')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	var locals = {
        section: 'component',
        user: req.user
    }

    var baseUri

    var meta
    var componentDefinition

    var encodedProteins = []
    var collections = []

    var otherComponents = []
    var mappings = {}

	const { graphUris, uri, designId } = getUrisFromReq(req)

    const graphUri = graphUris[0]

    getComponentDefinition(uri, graphUris).then((result) => {
        
        componentDefinition = result.object

        if(!componentDefinition || componentDefinition instanceof URI) {
            locals = {
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
                section: 'errors',
                user: req.user,
                errors: [ uri + ' summarizeComponentDefinition returned null' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return
        }

    }).then(function lookupEncodedProteins() {

        var query =
            'PREFIX sybio: <http://www.sybio.ncl.ac.uk#>\n' +
        'SELECT ?subject WHERE {' +
            '   ?subject sybio:en_by <' + uri + '>' +
            '}'

        return sparql.queryJson(query, graphUri).then((results) => {

            encodedProteins = results.map((result) => {
                return result.subject
            })

        })

    }).then(function lookupCollections() {

        return getContainingCollections(uri, graphUri).then((_collections) => {
            collections = _collections
        })

    }).then(function getOtherComponentMetaData() {

        if(meta.protein && meta.protein.encodedBy)
            otherComponents = otherComponents.concat(meta.protein.encodedBy)
                    /* todo and subcomponents */

        otherComponents = otherComponents.concat(encodedProteins)

        return Promise.all(otherComponents.map((otherComponent) => {
            
            return getComponentDefinitionMetadata(otherComponent, graphUris).then((metadata) => {

                componentNameMappings[otherComponent] = metadata[0].name

            })

        }))

    }).then(function renderView() {

	    var isDNA = 0

        meta.types = meta.types.map((type) => {

            if (type.description && type.description.name === 'DnaRegion') isDNA = 1

            return {
                uri: type.uri,
                term: type.uri,
                description: type.description
            }

        })

        meta.roles = meta.roles.map((role) => {

            var igemPrefix = 'http://synbiohub.org/terms/igem/partType/'

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
            meta.description = wiky.process(meta.description, {})
        }

        if (meta.igemDescription != '') {
            meta.igemDescription = wiky.process(meta.igemDescription, {})
        }

        if (meta.igemNotes != '') {
            meta.igemNotes = wiky.process(meta.igemNotes, {})
        }

        if (meta.source != '') {
            meta.source = wiky.process(meta.source, {})
        }

        if (meta.igemReplacedBy.uri != '') {
            meta.igemReplacedBy.uri = '/' + meta.igemReplacedBy.uri.toString().replace(config.get('databasePrefix'),'')
            meta.igemReplacedBy.id = meta.igemReplacedBy.uri.toString().replace('/public/','').replace('/1','') + ' '
            meta.igemReplacedBy.id = meta.igemReplacedBy.id.substring(meta.igemReplacedBy.id.indexOf('/')+1)
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
            } else {
                sequence.url = sequence.uri
            }
        })


        if(req.params.userId) {
            locals.sbolUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' + meta.id + '.xml'
            locals.fastaUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' + meta.id + '.fasta'
            locals.genBankUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/' + meta.id + '.gb'
            locals.searchUsesUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/uses'
            locals.searchTwinsUrl = '/user/' + encodeURIComponent(req.params.userId) + '/' + designId + '/twins'
        } else {
            locals.sbolUrl = '/public/' + designId + '/' + meta.id + '.xml'
            locals.fastaUrl = '/public/' + designId + '/' + meta.id + '.fasta'
            locals.genBankUrl = '/public/' + designId + '/' + meta.id + '.gb'
            locals.searchUsesUrl = '/public/' + designId + '/uses'
            locals.searchTwinsUrl = '/public/' + designId + '/twins'
        } 

        locals.keywords = []
        locals.citations = []
        locals.prefix = req.params.prefix

        if(meta.uri.indexOf('http://parts.igem.org/') === 0) {

            meta.experience = meta.uri + ':Experience'

        } 

        locals.encodedProteins = encodedProteins.map((uri) => {

            var prefixified = prefixify(uri, prefixes)

            return {
                uri: uri,
                name: mappings[uri],
                url: '/entry/' + prefixified.prefix + '/' + prefixified.uri
            }

        })

        locals.annotations = componentDefinition.annotations;
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
            } else {
                annotation.value = annotation.value.slice(annotation.value.lastIndexOf('/')+1)
            }
        })
        locals.annotations = locals.annotations.filter(function(annotation) {
            return (!annotation.name.toString().startsWith('created')) && (!annotation.name.toString().startsWith('modified')) && (!annotation.name.toString().startsWith('creator')) && (!annotation.name.toString().startsWith('source')) && (!annotation.name.toString().startsWith('description')) && (!annotation.name.toString().startsWith('notes')) && (!annotation.name.toString().startsWith('status')) && (!annotation.name.toString().startsWith('partStatus')) && (!annotation.name.toString().startsWith('results')) && (!annotation.name.toString().startsWith('category')) && (!annotation.name.toString().startsWith('dominant')) && (!annotation.name.toString().startsWith('favorite')) && (!annotation.name.toString().startsWith('discontinued')) && (!annotation.name.toString().startsWith('replacedBy')) && (!annotation.name.toString().startsWith('group_u_list')) && (!annotation.name.toString().startsWith('m_user_id')) && (!annotation.name.toString().startsWith('owner_id')) && (!annotation.name.toString().startsWith('owning_group_id') && (!annotation.name.toString().startsWith('rating')) && (!annotation.name.toString().startsWith('sampleStatus')))
        })

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
        }

        res.send(pug.renderFile('templates/views/componentDefinition.jade', locals))

    }).catch((err) => {

        const locals = {
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



