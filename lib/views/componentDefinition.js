

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const { getComponentDefinitionMetadata } = require('../query/component-definition')
const { getContainingCollections } = require('../query/local/collection')

var extend = require('xtend')

const shareImages = require('../shareImages')

var loadTemplate = require('../loadTemplate')

var retrieveCitations = require('../citations')

var sbolmeta = require('./utils/sbolmeta')

var formatSequence = require('sequence-formatter')

var async = require('async')

var prefixify = require('../prefixify')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var getDisplayList = require('visbol/lib/getDisplayList').getDisplayList

var wiky = require('../wiky/wiky.js');

var config = require('../config')

var striptags = require('striptags')

var URI = require('sboljs').URI

var sha1 = require('sha1');

var getUrisFromReq = require('../getUrisFromReq')

const uriToUrl = require('../uriToUrl')

const request = require('request')

var postprocess_igem = require('../postprocess_igem')

var generateDataRecord = require('../bioschemas/DataRecord')

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
    var remote

    var encodedProteins = []
    var collections = []

    var otherComponents = []
    var mappings = {}

    var builds = []

    var submissionCitations = []
    var citations = []

    var collectionIcon

    const { graphUri, uri, designId, share, url } = getUrisFromReq(req, res)

    fetchSBOLObjectRecursive('ComponentDefinition', uri, graphUri).then((result) => {

        sbol = result.sbol
        componentDefinition = result.object
        remote = result.remote || false

        if(!componentDefinition || componentDefinition instanceof URI) {
            return Promise.reject(new Error(uri + ' not found: ' + componentDefinition))
        }

        meta = sbolmeta.summarizeComponentDefinition(componentDefinition,req,sbol,remote,graphUri)

        if(!meta) {
            return Promise.reject(new Error('summarizeComponentDefinition returned null'))
        }

    }).then(function lookupCollections() {

        const DOIs = componentDefinition.getAnnotations('http://edamontology.org/data_1188')
        const pubmedIDs = componentDefinition.getAnnotations('http://purl.obolibrary.org/obo/OBI_0001617')

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

            retrieveCitations(pubmedIDs.map((pmid) => ({ citation: pmid }))).then((resolvedCitations) => {

                submissionCitations = resolvedCitations;

                //console.log('got citations ' + JSON.stringify(submissionCitations));

            })

        ])

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

    }).then(function lookupBuilds() {

        var templateParams = {
            uri: sparql.escapeIRI(uri)
        }

        var query = loadTemplate('sparql/GetImplementations.sparql', templateParams)

        return sparql.queryJson(query, graphUri).then((results) => {

            builds = results.map((result) => {
                return result.impl
            })

        })

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

    }).then(function fetchFromIgem() {

	if(componentDefinition.wasDerivedFrom.toString().indexOf('http://parts.igem.org/') === 0) {

	    return Promise.all([

		new Promise((resolve, reject) => {

		    request.get(componentDefinition.wasDerivedFrom.toString() + '?action=render', function(err, res, body) {

			if(err) {
			    resolve()
			    //reject(err)
			    return
			}

			if(res.statusCode >= 300) {
			    resolve()
			    //reject(new Error('HTTP ' + res.statusCode))
			    return
			}

			meta.iGemMainPage = body
			if (meta.iGemMainPage != '') {
			    meta.iGemMainPage = postprocess_igem(meta.iGemMainPage.toString())
			}

			resolve()
		    })
		}),


		new Promise((resolve, reject) => {

		    request.get(componentDefinition.wasDerivedFrom.toString() + ':Design?action=render', function(err, res, body) {

			if(err) {
			    //reject(err)
			    resolve()
			    return
			}

			if(res.statusCode >= 300) {
			    //reject(new Error('HTTP ' + res.statusCode))
			    resolve()
			    return
			}

			meta.iGemDesign = body
			if (meta.iGemDesign != '') {
			    meta.iGemDesign = postprocess_igem(meta.iGemDesign.toString())
			}

			resolve()
		    })
		}),


		new Promise((resolve, reject) => {

		    request.get(componentDefinition.wasDerivedFrom.toString() + ':Experience?action=render', function(err, res, body) {

			if(err) {
			    //reject(err)
			    resolve()
			    return
			}

			if(res.statusCode >= 300) {
			    //reject(new Error('HTTP ' + res.statusCode))
			    resolve()
			    return
			}

			meta.iGemExperience = body
			if (meta.iGemExperience != '') {
			    meta.iGemExperience = postprocess_igem(meta.iGemExperience.toString())
			}

			resolve()
		    })
		})

	    ])

	} else {
	    return Promise.resolve()
	}

    }).then(function renderView() {

//        var isDNA = 0

        meta.builds = builds

        locals.meta = meta

	locals.rdfType = {
	    name : 'Component',
	    url : 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Component'
	}
	locals.share = share
        locals.prefix = req.params.prefix

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
                if (sequence.uri.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
                    sequence.url += '/' + sha1('synbiohub_' + sha1(sequence.uri) + config.get('shareLinkSalt')) + '/share'
                }
            } else {
                sequence.url = sequence.uri
            }

            if(req.params.version === 'current') {
                sequence.url = sequence.url.toString().replace('/'+sequence.version, '/current')
                sequence.version = 'current'
            }
        })

	locals.BenchlingRemotes = (Object.keys(config.get('remotes')).filter(function(e){return config.get('remotes')[e].type ==='benchling'}).length > 0)
	locals.ICERemotes = (Object.keys(config.get('remotes')).filter(function(e){return config.get('remotes')[e].type ==='ice'}).length > 0)
        locals.removePublicEnabled = config.get('removePublicEnabled')

        locals.encodedProteins = encodedProteins.map((uri) => {

            return {
                uri: uri,
                name: mappings[uri],
                url: uri
            }

        })

        locals.metaDesc = striptags(locals.meta.description).trim()
        locals.title = locals.meta.name + ' ‒ ' + config.get('instanceName')

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

        locals.meta.displayList = getDisplayList(componentDefinition, config, req.url.toString().endsWith('/share'), 30)

	locals.meta.annotations.forEach((annotation) => {
	    if (annotation.name === 'benchling#edit' && req.params.version === 'current') {
		locals.remote = { name: 'Benchling',
				  url: annotation.url
				}
	    } else if (annotation.name === 'ice#entry' && req.params.version === 'current') {
		locals.remote = { name: 'ICE',
				  url: annotation.url
				}
	    }
	})

	locals.bioschemas = generateDataRecord(extend(locals.meta, { uri, rdfType:"https://bioschemas.org/BioChemEntity" }))
				// locals.bioschemas = generateDataRecord(extend(locals.meta, { uri }))

        locals.collections = collections
        locals.collectionIcon = collectionIcon
        locals.submissionCitations = submissionCitations
        locals.citationsSource = citations.map(function(citation) {
            return citation.citation
        }).join(',');

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
