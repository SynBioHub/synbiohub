
var pug = require('pug')

var sboljs = require('sboljs')
var async = require('async')

var search = require('../search')

var extend = require('xtend')

var escape = require('pg-escape')
    
var igemNS = 'http://wiki.synbiohub.org/wiki/Terms/igem#'

var biopaxNS = 'http://www.biopax.org/release/biopax-level3.owl#'

var soNS = 'http://identifiers.org/so/'

var config = require('../config')

var collNS = config.get('databasePrefix') + 'public/'

var sparql = require('../sparql/sparql')

const serializeSBOL = require('../serializeSBOL')

module.exports = function(req, res) {

    if(req.method === 'POST') {

        advancedSearchPost(req, res)

    } else {

        advancedSearchForm(req, res, {})

    }
	
}

function advancedSearchForm(req, res, properties, submissionData, locals) {

    var igemStatus = {}

    var submissionID = '';

    locals = extend({
        config: config.get(),
        section: 'advancedSearch',
        user: req.user,
        errors: []
    }, locals)

    getUriList(null).then((lists) => {

	var collectionMeta = { id: '', version: '', name: '', description: '' }

        locals = extend({
	    collectionMeta: collectionMeta,
            objectTypes: [{ name:'Collection'}, {name:'ComponentDefinition'}, {name:'Model'}, {name:'ModuleDefinition'}, {name:'Sequence'}],
            creators: lists.creatorList,
            partStatuses: lists.partStatusList,
            sampleStatuses: lists.sampleStatusList,
            statuses: lists.statusList,
            experiences: lists.experienceList,
            types: lists.typeList,
            roles: lists.roleList,
            igemRoles: lists.igemRoleList,
            collections: lists.collectionList
        }, locals)

	if (req.originalUrl.endsWith('/createCollection')) {
            res.send(pug.renderFile('templates/views/createCollection.jade', locals))    
	} else { 
            res.send(pug.renderFile('templates/views/advanced-search.jade', locals))    
	}

    }).catch((err) => {

        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })
}

function advancedSearchPost(req, res) {

    var limit = 50

    var criteriaStr = ''
    var criteria = []

    getUriList(null).then((lists) => {

        var mappedCreators = {};
        var query = '';
        if(req.body.objectType!=='Any Object Type') {
            criteriaStr += '   ?subject a sbol2:' + req.body.objectType + ' . '
            query += 'objectType='+req.body.objectType+'&'
        }
        for (var i = 0; i < lists.creatorList.length; i++) {
            mappedCreators[lists.creatorList[i].name] = lists.creatorList[i];
        } 
        if(req.body.creator!=='Any Creator') {
            criteriaStr += '   ?subject dc:creator \'' + mappedCreators[req.body.creator].uri + '\' . '
            query += 'dc:creator=\''+mappedCreators[req.body.creator].uri+'\'&'
        }
        if(req.body.createdAfter!=='' || req.body.createdBefore!=='') {
            criteriaStr += '   ?subject dcterms:created ?cdate . '
        }
        if(req.body.createdAfter!=='') {
            criteriaStr += '   FILTER (xsd:dateTime(?cdate) > "'+req.body.createdAfter+'T00:00:00"^^xsd:dateTime) '
            query += 'createdAfter='+req.body.createdAfter+'&'
        }
        if(req.body.createdBefore!=='') {
            criteriaStr += '   FILTER (xsd:dateTime(?cdate) < "'+req.body.createdBefore+'T00:00:00"^^xsd:dateTime) '
            query += 'createdBefore='+req.body.createdBefore+'&'
        }
        if(req.body.modifiedAfter!=='' || req.body.modifiedBefore!=='') {
            criteriaStr += '   ?subject dcterms:modified ?mdate . '
        }
        if(req.body.modifiedAfter!=='') {
            criteriaStr += '   FILTER (xsd:dateTime(?mdate) > "'+req.body.modifiedAfter+'T00:00:00"^^xsd:dateTime) '
            query += 'modifiedAfter='+req.body.modifiedAfter+'&'
        }
        if(req.body.modifiedBefore!=='') {
            criteriaStr += '   FILTER (xsd:dateTime(?mdate) < "'+req.body.modifiedBefore+'T00:00:00"^^xsd:dateTime) '
            query += 'modifiedBefore='+req.body.modifiedBefore+'&'
        }
        if(req.body.partStatus!=='Any iGEM Part Status') {
            criteriaStr += '   ?subject igem:partStatus \'' + req.body.partStatus.replace('\'','\\\'') + '\' . '
            query += 'igem:partStatus=\''+req.body.partStatus.replace('\'','\\\'')+'\'&'
        }
        if(req.body.sampleStatus!=='Any iGEM Sample Status') {
            criteriaStr += '   ?subject igem:sampleStatus \'' + req.body.sampleStatus.replace('\'','\\\'') + '\' . '
            query += 'igem:sampleStatus=\''+req.body.sampleStatus.replace('\'','\\\'')+'\'&'
        }
        if(req.body.status!=='Any iGEM Status') {
            criteriaStr += '   ?subject igem:status <' + igemNS + 'status/' + req.body.status + '> . '
            query += 'igem:status=<' + igemNS + 'status/' + req.body.status + '>&'
        }
        if(req.body.experience!=='Any iGEM Experience') {
            criteriaStr += '   ?subject igem:experience <' + igemNS + 'experience/' + req.body.experience + '> . '
            query += 'igem:experience=<' + igemNS + 'experience/' + req.body.experience + '>&'
        }
        if(req.body.type!=='Any Type') {
            criteriaStr += '   ?subject sbol2:type <' + biopaxNS + req.body.type + '> . '
            query += 'type=<' + biopaxNS + req.body.type + '>&'
        }
        var mappedRoles = {}; 
        for (var i = 0; i < lists.roleList.length; i++) {
            mappedRoles[lists.roleList[i].name] = lists.roleList[i];
        } 
        if(req.body.role!=='Any Role') {
            criteriaStr += '   ?subject sbol2:role <' +  mappedRoles[req.body.role].uri + '> . '
            query += 'role=<'+mappedRoles[req.body.role].uri+'>&'
        }
        if(req.body.partType!=='Any iGEM Part Type') {
            criteriaStr += '   ?subject sbol2:role <' + igemNS + 'partType/' + req.body.partType + '> . '
            query += 'role=<'+igemNS + 'partType/' + req.body.partType+'>&'
        }
        if(req.body.collection1) {
	    var collections = req.body.collection1.toString().split(',')
	    for (var i = 0; i < collections.length; i++) {
               criteriaStr +=           '   ?collection a sbol2:Collection .' +
		    '   <' + collNS + collections[i] + '> sbol2:member ?subject .'
		query += 'collection=<' + collNS + collections[i] + '>&'
            }
	}
        if(req.body.description!=='') {
            criteriaStr += escape(
            'FILTER (CONTAINS(lcase(?displayId), lcase(%L))||CONTAINS(lcase(?name), lcase(%L))||CONTAINS(lcase(?description), lcase(%L)))', 
            req.body.description,req.body.description,req.body.description
            )
        }
        query += req.body.description
        criteria.push(criteriaStr)

        var locals = {
            config: config.get(),
            section: 'search',
            user: req.user
        }

	if (req.originalUrl.endsWith('/createCollection')) {
	    limit = 10000
	}

        return search(null, criteria, req.query.offset, limit, req.user).then((searchRes) => {

            const count = searchRes.count
            const results = searchRes.results

	    if (req.originalUrl.endsWith('/createCollection')) {

		var sbol = new sboljs()
		var collection = sbol.collection()
		collection.displayId = req.body.metaId + '_collection'
		collection.version = req.body.metaVersion
		collection.persistentIdentity = config.get('databasePrefix') + 'user/' + req.user.username + '/' + req.body.metaId + '/' + collection.displayId
		collection.uri = collection.persistentIdentity + '/' + collection.version
		collection.name = req.body.metaName
		collection.description = req.body.metaDescription
		collection.addUriAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy', config.get('databasePrefix') + 'user/' + req.user.username)
		collection.addUriAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#topLevel', collection.uri)
		results.forEach((result) => {
		    collection.addMember(result.uri)
		})

		return sparql.upload(req.user.graphUri, serializeSBOL(sbol), 'application/rdf+xml').then(function redirectManage(next) {
		    return res.redirect('/manage');
		})
	    }

            locals.numResultsTotal = count

            locals.section = 'search';
            locals.searchQuery = query;
            locals.searchResults = results
            if (req.originalUrl.endsWith('/advancedSearch')) {
                locals.originalUrl = req.originalUrl + '/' + encodeURIComponent(query)
            } else if (req.originalUrl.indexOf("/?offset") !== -1) {
                locals.originalUrl = req.originalUrl.substring(0,req.originalUrl.indexOf("/?offset"))
            } else {
                locals.originalUrl = req.originalUrl
            }

            if (req.query.offset) {
                locals.firstResultNum = parseInt(req.query.offset) + 1
                if (count < parseInt(req.query.offset) + results.length) {
                    locals.lastResultNum = count
                } else { 
                    locals.lastResultNum = parseInt(req.query.offset) + results.length
                }
            } else {
                locals.firstResultNum = 1
                if (count < results.length) {
                    locals.lastResultNum = count
                } else { 
                    locals.lastResultNum = results.length
                }
            }

            if(results.length === 0)
                locals.firstResultNum = 0

            res.send(pug.renderFile('templates/views/search.jade', locals))
        })

    }).catch((err) => {

        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }

        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })
}

function getUriList(graphUri) {

    var creatorQuery = 'PREFIX dc: <http://purl.org/dc/elements/1.1/> SELECT DISTINCT ?object WHERE { ?tl dc:creator ?object }'

    var partStatusQuery = 'PREFIX igem: <http://wiki.synbiohub.org/wiki/Terms/igem#> SELECT DISTINCT ?object WHERE { ?tl igem:partStatus ?object }'

    var sampleStatusQuery = 'PREFIX igem: <http://wiki.synbiohub.org/wiki/Terms/igem#> SELECT DISTINCT ?object WHERE { ?tl igem:sampleStatus ?object }'

    var statusQuery = 'PREFIX igem: <http://wiki.synbiohub.org/wiki/Terms/igem#> SELECT DISTINCT ?object WHERE { ?tl igem:status ?object }'

    var experiencesQuery = 'PREFIX igem: <http://wiki.synbiohub.org/wiki/Terms/igem#> SELECT DISTINCT ?object WHERE { ?tl igem:experience ?object }'

    var typeQuery = 'PREFIX sbol2: <http://sbols.org/v2#> SELECT DISTINCT ?object WHERE { ?tl sbol2:type ?object }'

    var roleQuery = 'PREFIX sbol2: <http://sbols.org/v2#> SELECT DISTINCT ?object WHERE { ?tl a sbol2:ComponentDefinition .  ?tl sbol2:role ?object FILTER(STRSTARTS(str(?object),\'http://identifiers.org/so/\')) }'

    var igemRoleQuery = 'PREFIX sbol2: <http://sbols.org/v2#> SELECT DISTINCT ?object WHERE { ?tl a sbol2:ComponentDefinition . ?tl sbol2:role ?object FILTER(STRSTARTS(str(?object),\'http://wiki.synbiohub.org/wiki/Terms/igem#partType/\')) }'

    var collectionQuery = 'PREFIX sbol2: <http://sbols.org/v2#> SELECT DISTINCT ?object WHERE { ?object a sbol2:Collection }'

    function sortByNames(a, b) {
        if (a.name < b.name) {
            return -1
        } else {
            return 1
        }
    }

    return Promise.all([

	    sparql.queryJson(creatorQuery, graphUri).then((creatorList) => {

            creatorList.forEach((result) => {
                result.uri = result.object
                result.name = result.object
                delete result.object
            })	

            creatorList.sort(sortByNames)

            return Promise.resolve({ creatorList: creatorList })
	    }),

	    sparql.queryJson(partStatusQuery, graphUri).then((partStatusList) => {

            partStatusList.forEach((result) => {
                result.uri = result.object
                delete result.object
                result.name = result.uri
            })	

            partStatusList.sort(sortByNames)

            return Promise.resolve({ partStatusList: partStatusList })
        }),

        sparql.queryJson(sampleStatusQuery, graphUri).then((sampleStatusList) => {

            sampleStatusList.forEach((result) => {
                result.uri = result.object
                delete result.object
                result.name = result.uri
            })	

            sampleStatusList.sort(sortByNames)

            return Promise.resolve({ sampleStatusList: sampleStatusList })
        }),

	    sparql.queryJson(statusQuery, graphUri).then((statusList) => {

            statusList.forEach((result) => {
                result.uri = result.object
                delete result.object
                result.name = result.uri.toString().replace(igemNS+'status/','')
            })	

            statusList.sort(sortByNames)

            return Promise.resolve({ statusList: statusList })
	    }),

	    sparql.queryJson(experiencesQuery, graphUri).then((experienceList) => {

            experienceList.forEach((result) => {
                result.uri = result.object
                delete result.object
                result.name = result.uri.toString().replace(igemNS+'experience/','')
            })

            experienceList.sort(sortByNames)

            return Promise.resolve({ experienceList: experienceList })
	    }),

        sparql.queryJson(typeQuery, graphUri).then((typeList) => {

            typeList.forEach((result) => {
                result.uri = result.object
                delete result.object
                result.name = result.uri.toString().replace(biopaxNS,'')
            })

            typeList.sort(sortByNames)

            return Promise.resolve({ typeList: typeList })
        }),

        sparql.queryJson(roleQuery, graphUri).then((roleList) => {

            roleList.forEach((result) => {
                result.uri = result.object
                delete result.object
                var soTerm = result.uri.toString().replace(soNS,'')
                var sequenceOntology = require('../ontologies/sequence-ontology')
                result.name = sequenceOntology[soTerm].name
            })

            roleList.sort(sortByNames)

            return Promise.resolve({ roleList: roleList })
		}),

        sparql.queryJson(igemRoleQuery, graphUri).then((igemRoleList) => {
            igemRoleList.forEach((result) => {
                result.uri = result.object
                delete result.object
                result.name = result.uri.toString().replace(igemNS+'partType/','')
            })

            igemRoleList.sort(sortByNames)

            return Promise.resolve({ igemRoleList: igemRoleList })
        }),

        sparql.queryJson(collectionQuery, graphUri).then((collectionList) => {

            collectionList.forEach((result) => {
                result.uri = result.object
                delete result.object
                result.name = result.uri.toString().replace(collNS,'')
            })

            collectionList.sort(sortByNames)

            return Promise.resolve({ collectionList: collectionList })
        })

    ]).then((results) => {

        var allResults = {}

        results.forEach((resultObj) => {
            Object.keys(resultObj).forEach((key) => {
                allResults[key] = resultObj[key]
            })
        })

        return Promise.resolve(allResults)
    })
}

