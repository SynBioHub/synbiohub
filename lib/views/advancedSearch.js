
var pug = require('pug')

var sboljs = require('sboljs')
var async = require('async')

var stack = require('../stack')()
var search = require('../search')

var extend = require('xtend')

var escape = require('pg-escape')
    
var igemNS = 'http://synbiohub.org/terms/igem/'

var biopaxNS = 'http://www.biopax.org/release/biopax-level3.owl#'

var soNS = 'http://identifiers.org/so/'
    
var collNS = 'http://synbiohub.org/public/'

module.exports = function(req, res) {

    if(req.method === 'POST') {

        advancedSearchPost(req, res)

    } else {

	advancedSearchForm(req, res, {})

    }
	
}

function getUriList(store, callback) {

    var creatorQuery = 'PREFIX dcterms: <http://purl.org/dc/terms/> SELECT DISTINCT ?object WHERE { ?tl dcterms:creator ?object }'

    var partStatusQuery = 'PREFIX igem: <http://synbiohub.org/terms/igem/> SELECT DISTINCT ?object WHERE { ?tl igem:partStatus ?object }'

    var statusQuery = 'PREFIX igem: <http://synbiohub.org/terms/igem/> SELECT DISTINCT ?object WHERE { ?tl igem:status ?object }'

    var resultsQuery = 'PREFIX igem: <http://synbiohub.org/terms/igem/> SELECT DISTINCT ?object WHERE { ?tl igem:results ?object }'

    var typeQuery = 'PREFIX sbol2: <http://sbols.org/v2#> SELECT DISTINCT ?object WHERE { ?tl sbol2:type ?object }'

    var roleQuery = 'PREFIX sbol2: <http://sbols.org/v2#> SELECT DISTINCT ?object WHERE { ?tl a sbol2:ComponentDefinition .  ?tl sbol2:role ?object FILTER(STRSTARTS(str(?object),\'http://identifiers.org/so/\')) }'

    var igemRoleQuery = 'PREFIX sbol2: <http://sbols.org/v2#> SELECT DISTINCT ?object WHERE { ?tl a sbol2:ComponentDefinition . ?tl sbol2:role ?object FILTER(STRSTARTS(str(?object),\'http://synbiohub.org/terms/igem/partType/\')) }'

    var collectionQuery = 'PREFIX sbol2: <http://sbols.org/v2#> SELECT DISTINCT ?object WHERE { ?object a sbol2:Collection }'

    var statusList = []

    var resultList = []

    async.parallel({
       one: function getCreatorList(next) {
	    store.sparql(creatorQuery, (err, creatorList) => {
		if(err) {
		    callback(err)
		} else {
		    creatorList.map((result) => {
			result.uri = result.object
			if (result.object.toString().indexOf(',') > 0) {
			    result.name = result.object.toString().substring(0,result.object.toString().indexOf(',')) + ' et al.'
			} else {
			    result.name = result.object
			}
			delete result.object
			return result
		    })	
		    creatorList.sort(function(a,b){ if (a.name < b.name) { return -1 } else { return 1 } })
		    next(null, creatorList)
		}

	    })
	},
        twoA: function getPartStatusList(next) {
	    store.sparql(partStatusQuery, (err, partStatusList) => {
		if(err) {
		    callback(err)
		} else {
		    partStatusList.map((result) => {
			result.uri = result.object
			delete result.object
			result.name = result.uri
			return result
		    })	
		    partStatusList.sort(function(a,b){ if (a.name < b.name) { return -1 } else { return 1 } })
		    next(null, partStatusList)
		}

	    })
	},
        two: function getStatusList(next) {
	    store.sparql(statusQuery, (err, statusList) => {
		if(err) {
		    callback(err)
		} else {
		    statusList.map((result) => {
			result.uri = result.object
			delete result.object
			result.name = result.uri.toString().replace(igemNS+'status/','')
			return result
		    })	
		    statusList.sort(function(a,b){ if (a.name < b.name) { return -1 } else { return 1 } })
		    next(null, statusList)
		}

	    })
	},
        three: function getResultList(next) {
	    store.sparql(resultsQuery, (err, resultList) => {
		if(err) {
		    callback(err)
		} else {
		    resultList.map((result) => {
			result.uri = result.object
			delete result.object
			result.name = result.uri.toString().replace(igemNS+'results/','')
			return result
		    })
		    resultList.sort(function(a,b){ if (a.name < b.name) { return -1 } else { return 1 } })
		    next(null, resultList)
		}
	    })
	},
        four: function getTypeList(next) {
	    store.sparql(typeQuery, (err, typeList) => {
		if(err) {
		    callback(err)
		} else {
		    typeList.map((result) => {
			result.uri = result.object
			delete result.object
			result.name = result.uri.toString().replace(biopaxNS,'')
			return result
		    })
		    typeList.sort(function(a,b){ if (a.name < b.name) { return -1 } else { return 1 } })
		    next(null, typeList)
		}
	    })
	},
        five: function getRoleList(next) {
	    store.sparql(roleQuery, (err, roleList) => {
		if(err) {
		    callback(err)
		} else {
		    roleList.map((result) => {
			result.uri = result.object
			delete result.object
			var soTerm = result.uri.toString().replace(soNS,'')
			var sbolmeta = require('sbolmeta')
			var sequenceOntology = sbolmeta.sequenceOntology
			result.name = sequenceOntology[soTerm].name
			return result
		    })
		    roleList.sort(function(a,b){ if (a.name < b.name) { return -1 } else { return 1 } })
		    next(null, roleList)
		}
	    })
	},        
	six: function getIgemRoleList(next) {
	    store.sparql(igemRoleQuery, (err, igemRoleList) => {
		if(err) {
		    callback(err)
		} else {
		    igemRoleList.map((result) => {
			result.uri = result.object
			delete result.object
			result.name = result.uri.toString().replace(igemNS+'partType/','')
			return result
		    })
		    igemRoleList.sort(function(a,b){ if (a.name < b.name) { return -1 } else { return 1 } })
		    next(null, igemRoleList)
		}
	    })
	},        
	seven: function getCollectionList(next) {
	    store.sparql(collectionQuery, (err, collectionList) => {
		if(err) {
		    callback(err)
		} else {
		    collectionList.map((result) => {
			result.uri = result.object
			delete result.object
			result.name = result.uri.toString().replace(collNS,'').replace('_collection/1','').replace('collection/1','')
			return result
		    })
		    collectionList.sort(function(a,b){ if (a.name < b.name) { return -1 } else { return 1 } })
		    next(null, collectionList)
		}
	    })
	}
    }, function(err, results) {
	callback(null, results.one, results.twoA, results.two, results.three, results.four, results.five, results.six, results.seven)
    })
}

function advancedSearchForm(req, res, properties, submissionData, locals) {

    var igemStatus = {}
	
  var submissionID = '';

    locals = extend({
        section: 'submit',
        user: req.user,
        errors: []
    }, locals)

    getUriList(stack.getDefaultStore(), (err, creatorList, partStatusList, statusList, resultList, typeList, roleList, igemRoleList, collectionList) => {

        if(err) {
	    locals = {
                section: 'errors',
                user: req.user,
                errors: [ err ]
	    }
	    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
	    return        
        }
	locals = extend({
	    objectTypes: [{ name:'Collection'}, {name:'ComponentDefinition'}, {name:'Model'}, {name:'ModuleDefinition'}, {name:'Sequence'}],
	    creators: creatorList,
            partStatuses: partStatusList,
            statuses: statusList,
            results: resultList,
	    types: typeList,
	    roles: roleList,
	    igemRoles: igemRoleList,
	    collections: collectionList
	}, locals)
	res.send(pug.renderFile('templates/views/advanced-search.jade', locals))    
    }) 
}

function advancedSearchPost(req, res) {

    var limit = 50

    var criteriaStr = ''
    var criteria = []

    getUriList(stack.getDefaultStore(), (err, creatorList, partStatusList, statusList, resultList, typeList, roleList, igemRoleList, collectionList) => {
	var mappedCreators = {};
	var query = '';
	console.log(req.body)
	if(req.body.objectType!=='Any Object Type') {
	    criteriaStr += '   ?subject a sbol2:' + req.body.objectType + ' . '
	    query += 'objectType='+req.body.objectType+'&'
	}
	for (var i = 0; i < creatorList.length; i++) {
	    mappedCreators[creatorList[i].name] = creatorList[i];
	} 
	if(req.body.creator!=='Any Creator') {
	    criteriaStr += '   ?subject dcterms:creator \'' + mappedCreators[req.body.creator].uri + '\' . '
	    query += 'dcterms:creator=\''+mappedCreators[req.body.creator].uri+'\'&'
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
	    criteriaStr += '   ?subject igem:partStatus \'' + req.body.partStatus + '\' . '
	    query += 'igem:partStatus=\''+req.body.partStatus+'\'&'
	}
	if(req.body.status!=='Any iGEM Status') {
	    criteriaStr += '   ?subject igem:status <' + igemNS + 'status/' + req.body.status + '> . '
	    query += 'igem:status=<' + igemNS + 'status/' + req.body.status + '>&'
	}
	if(req.body.results!=='Any iGEM Results') {
	    criteriaStr += '   ?subject igem:results <' + igemNS + 'results/' + req.body.results + '> . '
	    query += 'igem:results=<' + igemNS + 'results/' + req.body.results + '>&'
	}
	if(req.body.type!=='Any Type') {
	    criteriaStr += '   ?subject sbol2:type <' + biopaxNS + req.body.type + '> . '
	    query += 'type=<' + biopaxNS + req.body.type + '>&'
	}
	var mappedRoles = {}; 
	for (var i = 0; i < roleList.length; i++) {
	    mappedRoles[roleList[i].name] = roleList[i];
	} 
	if(req.body.role!=='Any Role') {
	    criteriaStr += '   ?subject sbol2:role <' +  mappedRoles[req.body.role].uri + '> . '
	    query += 'role=<'+mappedRoles[req.body.role].uri+'>&'
	}
	if(req.body.partType!=='Any iGEM Part Type') {
	    criteriaStr += '   ?subject sbol2:role <' + igemNS + 'partType/' + req.body.partType + '> . '
	    query += 'role=<'+igemNS + 'partType/' + req.body.partType+'>&'
	}
	if(req.body.collection1!=='Any Collection') {
	    criteriaStr +=           '   ?collection a sbol2:Collection .' +
		'   <' + collNS + req.body.collection1 + '_collection/1> sbol2:member ?subject .'
	    query += 'collection=<' + collNS + req.body.collection1 + '_collection/1>&'
	}
	if(req.body.collection2!=='Any Collection') {
	    criteriaStr +=           '   ?collection a sbol2:Collection .' +
		'   <' + collNS + req.body.collection2 + '_collection/1> sbol2:member ?subject .'
	    query += 'collection=<' + collNS + req.body.collection2 + '_collection/1>&'
	}
	if(req.body.collection3!=='Any Collection') {
	    criteriaStr +=           '   ?collection a sbol2:Collection .' +
		'   <' + collNS + req.body.collection3 + '_collection/1> sbol2:member ?subject .'
	    query += 'collection=<' + collNS + req.body.collection3 + '_collection/1>&'
	}
	if(req.body.description!=='') {
	    criteriaStr += escape(
		'FILTER (CONTAINS(?displayId, %L)||CONTAINS(?name, %L)||CONTAINS(?description, %L))', 
		req.body.description,req.body.description,req.body.description
	    )
	}
	query += req.body.description
	criteria.push(criteriaStr)

	var locals = {
        section: 'search',
        user: req.user
	}

	stack.getPrefixes((err, prefixes) => {

            if(err) {
		locals = {
                    section: 'errors',
                    user: req.user,
                    errors: [ err ]
		}
		res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
		return        
            }

            search(stack.getDefaultStore(), prefixes, criteria, req.query.offset, limit, (err, count, results) => {

		if(err) {
		    locals = {
			section: 'errors',
			user: req.user,
			errors: [ err ]
		    }
		    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
		    return        
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
	}) 
    })
};
