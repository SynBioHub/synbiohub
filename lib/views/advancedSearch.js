
var pug = require('pug')

var Sboljs = require('sboljs')

var search = require('../search')

var extend = require('xtend')

var escape = require('pg-escape')

var igemNS = 'http://wiki.synbiohub.org/wiki/Terms/igem#'

var biopaxNS = 'http://www.biopax.org/release/biopax-level3.owl#'

var soNS = 'http://identifiers.org/so/'

var sbolNS = 'http://sbols.org/v2#'

var provNS = 'http://www.w3.org/ns/prov#'

var sbhNS = 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#'

var dctermsNS = 'http://purl.org/dc/terms/'

var dcNS = 'http://purl.org/dc/elements/1.1/'

var celloNS = 'http://cellocad.org/Terms/cello#'

var rdfNS = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'

var rdfsNS = 'http://www.w3.org/2000/01/rdf-schema#'

var purlNS = 'http://purl.obolibrary.org/obo/'

var genbankNS = 'http://www.ncbi.nlm.nih.gov/genbank#'

var config = require('../config')

var sparql = require('../sparql/sparql')

const serializeSBOL = require('../serializeSBOL')

var loadTemplate = require('../loadTemplate')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    if (req.body.adv === 'Search') {
      advancedSearchPost(req, res)
    } else {
      advancedSearchForm(req, res, {})
    }
  } else {
    advancedSearchForm(req, res, {})
  }
}

function shortName (uri) {
  var name
  if (uri.toString().startsWith(igemNS)) {
    name = uri.replace(igemNS, 'igem:')
  } else if (uri.toString().startsWith(provNS)) {
    name = uri.replace(provNS, 'prov:')
  } else if (uri.toString().startsWith(sbolNS)) {
    name = uri.replace(sbolNS, 'sbol2:')
  } else if (uri.toString().startsWith(sbhNS)) {
    name = uri.replace(sbhNS, 'sbh:')
  } else if (uri.toString().startsWith(celloNS)) {
    name = uri.replace(celloNS, 'cello:')
  } else if (uri.toString().startsWith(dctermsNS)) {
    name = uri.replace(dctermsNS, 'dcterms:')
  } else if (uri.toString().startsWith(dcNS)) {
    name = uri.replace(dcNS, 'dc:')
  } else if (uri.toString().startsWith(rdfNS)) {
    name = uri.replace(rdfNS, 'rdf:')
  } else if (uri.toString().startsWith(rdfsNS)) {
    name = uri.replace(rdfsNS, 'rdfs:')
  } else if (uri.toString().startsWith(purlNS)) {
    name = uri.replace(purlNS, 'purl:')
  } else if (uri.toString().startsWith(genbankNS)) {
    name = uri.replace(genbankNS, 'genbank:')
  } else {
    name = uri
  }
  return name
}

function longName (name) {
  var uri
  if (name.toString().startsWith('igem:')) {
    uri = name.replace('igem:', igemNS)
  } else if (name.toString().startsWith('prov:')) {
    uri = name.replace('prov:', provNS)
  } else if (name.toString().startsWith('sbol2:')) {
    uri = name.replace('sbol2:', sbolNS)
  } else if (name.toString().startsWith('sbh:')) {
    uri = name.replace('sbh:', sbhNS)
  } else if (name.toString().startsWith('cello:')) {
    uri = name.replace('cello:', celloNS)
  } else if (name.toString().startsWith('dcterms:')) {
    uri = name.replace('dcterms:', dctermsNS)
  } else if (name.toString().startsWith('dc:')) {
    uri = name.replace('dc:', dcNS)
  } else if (name.toString().startsWith('rdf:')) {
    uri = name.replace('rdf:', rdfNS)
  } else if (name.toString().startsWith('rdfs:')) {
    uri = name.replace('rdfs:', rdfsNS)
  } else if (name.toString().startsWith('purl:')) {
    uri = name.replace('purl:', purlNS)
  } else if (name.toString().startsWith('genbank:')) {
    uri = name.replace('genbank:', genbankNS)
  } else {
    uri = name
  }
  return uri
}

function advancedSearchForm (req, res, locals) {
  locals = extend({
    config: config.get(),
    section: 'advancedSearch',
    user: req.user,
    errors: []
  }, locals)

  getUriList(req, null).then((lists) => {
    var collectionMeta = { id: '', version: '', name: '', description: '' }

    locals = extend({
      collectionMeta: collectionMeta,
      objectTypes: lists.typesList,
      initObjectType: req.body.objectType ? req.body.objectType : 'No Filter',
      creators: lists.creatorList,
      initCreator: req.body.creator ? req.body.creator : 'No Filter',
      createdAfter: req.body.createdAfter,
      createdBefore: req.body.createdBefore,
      modifiedAfter: req.body.modifiedAfter,
      modifiedBefore: req.body.modifiedBefore,
      description: req.body.description,
      types: lists.typeList,
      initType: req.body.type ? req.body.type : 'No Filter',
      roles: lists.roleList,
      initRole: req.body.role ? req.body.role : 'No Filter',
      collections: lists.collectionList,
      initCollections: req.body.collections,
      predicates: lists.predicateList,
      initPred1: req.body.searchPred1 ? req.body.searchPred1 : 'No Filter',
      initPred2: req.body.searchPred2 ? req.body.searchPred2 : 'No Filter',
      initPred3: req.body.searchPred3 ? req.body.searchPred3 : 'No Filter',
      initPred4: req.body.searchPred4 ? req.body.searchPred4 : 'No Filter',
      initPred5: req.body.searchPred5 ? req.body.searchPred5 : 'No Filter',
      initObj1: req.body.filterObj1 ? req.body.filterObj1 : 'No Filter',
      initObj2: req.body.filterObj2 ? req.body.filterObj2 : 'No Filter',
      initObj3: req.body.filterObj3 ? req.body.filterObj3 : 'No Filter',
      initObj4: req.body.filterObj4 ? req.body.filterObj4 : 'No Filter',
      initObj5: req.body.filterObj5 ? req.body.filterObj5 : 'No Filter',
      objects1: lists.object1List,
      objects2: lists.object2List,
      objects3: lists.object3List,
      objects4: lists.object4List,
      objects5: lists.object5List
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
    console.error(err.stack)
    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
  })
}

function getQuery (searchPred, searchObj, filterObj, criteriaStr, queryStr) {
  if (searchPred !== 'No Filter' && searchObj !== 'No Filter') {
    var predStr = searchPred
    if (!searchPred.toString().startsWith('sbol2:')) {
      predStr = searchPred.replace('sbol2:', '')
    }
    if (searchPred === 'a') {
      predStr = 'objectType'
    }
    var objStr
    if (searchObj) {
      objStr = searchObj
    } else {
      objStr = filterObj
    }
    objStr = longName(objStr)
    if (objStr.toString().startsWith('http')) {
      objStr = '<' + objStr + '>'
    } else if (objStr.toString().includes(':')) {
      // Do nothing
    } else {
      objStr = '\'' + objStr + '\''
    }
    criteriaStr += '   ?subject ' + searchPred + ' ' + objStr + ' .'
    queryStr += predStr + '=' + objStr + '&'
  }
  return { criteriaStr: criteriaStr, queryStr: queryStr }
}

// TODO: allow non-top-level search
function advancedSearchPost (req, res) {
  var limit = 50

  getUriList(req, null).then((lists) => {
    var mappedCreators = {}
    var criteria = []

    var query = getQuery('a', req.body.objectType, null, '', '')
    for (var i = 0; i < lists.creatorList.length; i++) {
      mappedCreators[lists.creatorList[i].name] = lists.creatorList[i]
    }
    if (req.body.creator !== 'No Filter') {
      query = getQuery('dc:creator', mappedCreators[req.body.creator].uri, null, query.criteriaStr, query.queryStr)
    }
    if (req.body.createdAfter !== '' || req.body.createdBefore !== '') {
      query.criteriaStr = query.criteriaStr += '   ?subject dcterms:created ?cdate . '
    }
    if (req.body.createdAfter !== '') {
      query.criteriaStr += '   FILTER (xsd:dateTime(?cdate) > "' + req.body.createdAfter + 'T00:00:00"^^xsd:dateTime) '
      query.queryStr += 'createdAfter=' + req.body.createdAfter + '&'
    }
    if (req.body.createdBefore !== '') {
      query.criteriaStr += '   FILTER (xsd:dateTime(?cdate) < "' + req.body.createdBefore + 'T00:00:00"^^xsd:dateTime) '
      query.queryStr += 'createdBefore=' + req.body.createdBefore + '&'
    }
    if (req.body.modifiedAfter !== '' || req.body.modifiedBefore !== '') {
      query.criteriaStr += '   ?subject dcterms:modified ?mdate . '
    }
    if (req.body.modifiedAfter !== '') {
      query.criteriaStr += '   FILTER (xsd:dateTime(?mdate) > "' + req.body.modifiedAfter + 'T00:00:00"^^xsd:dateTime) '
      query.queryStr += 'modifiedAfter=' + req.body.modifiedAfter + '&'
    }
    if (req.body.modifiedBefore !== '') {
      query.criteriaStr += '   FILTER (xsd:dateTime(?mdate) < "' + req.body.modifiedBefore + 'T00:00:00"^^xsd:dateTime) '
      query.queryStr += 'modifiedBefore=' + req.body.modifiedBefore + '&'
    }
    query = getQuery(req.body.searchPred1, req.body.searchObj1, req.body.filterObj1, query.criteriaStr, query.queryStr)
    query = getQuery(req.body.searchPred2, req.body.searchObj2, req.body.filterObj2, query.criteriaStr, query.queryStr)
    query = getQuery(req.body.searchPred3, req.body.searchObj3, req.body.filterObj3, query.criteriaStr, query.queryStr)
    query = getQuery(req.body.searchPred4, req.body.searchObj4, req.body.filterObj4, query.criteriaStr, query.queryStr)
    query = getQuery(req.body.searchPred5, req.body.searchObj5, req.body.filterObj5, query.criteriaStr, query.queryStr)
    if (req.body.type !== 'No Filter') {
      if (req.body.type.toString().startsWith('http')) {
        query = getQuery('sbol2:type', req.body.type, null, query.criteriaStr, query.queryStr)
      } else {
        query = getQuery('sbol2:type', biopaxNS + req.body.type, null, query.criteriaStr, query.queryStr)
      }
    }
    var mappedRoles = {}
    for (var roleIndex = 0; roleIndex < lists.roleList.length; roleIndex++) {
      mappedRoles[lists.roleList[roleIndex].name] = lists.roleList[roleIndex]
    }
    if (req.body.role !== 'No Filter') {
      if (req.body.role.toString().startsWith('http')) {
        query = getQuery('sbol2:role', req.body.role, null, query.criteriaStr, query.queryStr)
      } else if (req.body.role.toString().startsWith('igem:')) {
        query = getQuery('sbol2:role', igemNS + 'partType/' + req.body.role.replace('igem:', ''), null, query.criteriaStr, query.queryStr)
      } else {
        query = getQuery('sbol2:role', mappedRoles[req.body.role].uri, null, query.criteriaStr, query.queryStr)
      }
    }
    var mappedCollections = {}
    for (var collectionIndex = 0; collectionIndex < lists.collectionList.length; collectionIndex++) {
      mappedCollections[lists.collectionList[collectionIndex].name] = lists.collectionList[collectionIndex]
    }
    if (req.body.collections) {
      var collections = req.body.collections.toString().split(',')
      for (collectionIndex = 0; collectionIndex < collections.length; collectionIndex++) {
        query.criteriaStr += '   ?collection a sbol2:Collection .' +
'   <' + mappedCollections[collections[collectionIndex]].uri + '> sbol2:member ?subject .'
        query.queryStr += 'collection=<' + mappedCollections[collections[collectionIndex]].uri + '>&'
      }
    }
    if (req.body.description !== '') {
      query.criteriaStr += escape(
        'FILTER (CONTAINS(lcase(?displayId), lcase(%L))||CONTAINS(lcase(?name), lcase(%L))||CONTAINS(lcase(?description), lcase(%L)))',
        req.body.description, req.body.description, req.body.description
      )
    }
    query.queryStr += req.body.description
    criteria.push(query.criteriaStr)

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
        var sbol = new Sboljs()
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

        return sparql.upload(req.user.graphUri, serializeSBOL(sbol), 'application/rdf+xml').then(function redirectManage (next) {
          return res.redirect('/manage')
        })
      }

      locals.numResultsTotal = count

      locals.section = 'search'
      locals.searchQuery = query.queryStr
      locals.searchResults = results
      if (req.originalUrl.endsWith('/advancedSearch')) {
        locals.originalUrl = req.originalUrl + '/' + encodeURIComponent(query.queryStr)
      } else if (req.originalUrl.indexOf('/?offset') !== -1) {
        locals.originalUrl = req.originalUrl.substring(0, req.originalUrl.indexOf('/?offset'))
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

      if (results.length === 0) { locals.firstResultNum = 0 }

      res.send(pug.renderFile('templates/views/search.jade', locals))
    })
  }).catch((err) => {
    var errorlocals = {
      config: config.get(),
      section: 'errors',
      user: req.user,
      errors: [ err ]
    }
    console.error(err.stack)
    res.send(pug.renderFile('templates/views/errors/errors.jade', errorlocals))
  })
}

function getUriList (req, graphUri) {
  var templateParams = {
    from: req.user ? 'FROM <' + config.get('databasePrefix') + 'public>' + ' FROM <' + req.user.graphUri + '>' : ''
  }
  var typesQuery = loadTemplate('sparql/getTypes.sparql', templateParams)
  var creatorQuery = loadTemplate('sparql/getCreators.sparql', templateParams)
  var typeQuery = loadTemplate('sparql/getSBOLTypes.sparql', templateParams)
  var roleQuery = loadTemplate('sparql/getRoles.sparql', templateParams)
  var predicateQuery = loadTemplate('sparql/getPredicates.sparql', templateParams)
  var collectionQuery = loadTemplate('sparql/getCollections.sparql', templateParams)

  function sortByNames (a, b) {
    if (a.name.toLowerCase() < b.name.toLowerCase()) {
      return -1
    } else {
      return 1
    }
  }

  return Promise.all([

    new Promise((resolve, reject) => {
      if (req.body.searchPred1 && req.body.searchPred1 !== 'No Filter') {
        templateParams.predicate = req.body.searchPred1.includes(':') ? req.body.searchPred1 : 'sbol2:' + req.body.searchPred1
        var objectQuery1 = loadTemplate('sparql/searchObj.sparql', templateParams)
        console.debug(objectQuery1)
        sparql.queryJson(objectQuery1, graphUri).then((object1List) => {
          object1List.forEach((result) => {
            result.uri = result.object
            delete result.object
            result.name = shortName(result.uri)
          })
          object1List.sort(sortByNames)
          resolve({ object1List: object1List })
        })
      } else {
        resolve({ object1List: {} })
      }
    }),

    new Promise((resolve, reject) => {
      if (req.body.searchPred2 && req.body.searchPred2 !== 'No Filter') {
        templateParams.predicate = req.body.searchPred2.includes(':') ? req.body.searchPred2 : 'sbol2:' + req.body.searchPred2
        var objectQuery2 = loadTemplate('sparql/searchObj.sparql', templateParams)
        console.debug(objectQuery2)
        sparql.queryJson(objectQuery2, graphUri).then((object2List) => {
          object2List.forEach((result) => {
            result.uri = result.object
            delete result.object
            result.name = shortName(result.uri)
          })
          object2List.sort(sortByNames)
          resolve({ object2List: object2List })
        })
      } else {
        resolve({ object2List: {} })
      }
    }),

    new Promise((resolve, reject) => {
      if (req.body.searchPred3 && req.body.searchPred3 !== 'No Filter') {
        templateParams.predicate = req.body.searchPred3.includes(':') ? req.body.searchPred3 : 'sbol3:' + req.body.searchPred3
        var objectQuery3 = loadTemplate('sparql/searchObj.sparql', templateParams)
        console.debug(objectQuery3)
        sparql.queryJson(objectQuery3, graphUri).then((object3List) => {
          object3List.forEach((result) => {
            result.uri = result.object
            delete result.object
            result.name = shortName(result.uri)
          })
          object3List.sort(sortByNames)
          resolve({ object3List: object3List })
        })
      } else {
        resolve({ object3List: {} })
      }
    }),

    new Promise((resolve, reject) => {
      if (req.body.searchPred4 && req.body.searchPred4 !== 'No Filter') {
        templateParams.predicate = req.body.searchPred4.includes(':') ? req.body.searchPred4 : 'sbol4:' + req.body.searchPred4
        var objectQuery4 = loadTemplate('sparql/searchObj.sparql', templateParams)
        console.debug(objectQuery4)
        sparql.queryJson(objectQuery4, graphUri).then((object4List) => {
          object4List.forEach((result) => {
            result.uri = result.object
            delete result.object
            result.name = shortName(result.uri)
          })
          object4List.sort(sortByNames)
          resolve({ object4List: object4List })
        })
      } else {
        resolve({ object4List: {} })
      }
    }),

    new Promise((resolve, reject) => {
      if (req.body.searchPred5 && req.body.searchPred5 !== 'No Filter') {
        templateParams.predicate = req.body.searchPred5.includes(':') ? req.body.searchPred5 : 'sbol5:' + req.body.searchPred5
        var objectQuery5 = loadTemplate('sparql/searchObj.sparql', templateParams)
        console.debug(objectQuery5)
        sparql.queryJson(objectQuery5, graphUri).then((object5List) => {
          object5List.forEach((result) => {
            result.uri = result.object
            delete result.object
            result.name = shortName(result.uri)
          })
          object5List.sort(sortByNames)
          resolve({ object5List: object5List })
        })
      } else {
        resolve({ object5List: {} })
      }
    }),

    sparql.queryJson(typesQuery, graphUri).then((typesList) => {
      typesList.forEach((result) => {
        result.uri = result.object
        delete result.object
        result.name = shortName(result.uri)
      })

      typesList.sort(sortByNames)

      return Promise.resolve({ typesList: typesList })
    }),

    sparql.queryJson(creatorQuery, graphUri).then((creatorList) => {
      creatorList.forEach((result) => {
        result.uri = result.object
        delete result.object
        result.name = result.uri
      })

      creatorList.sort(sortByNames)

      return Promise.resolve({ creatorList: creatorList })
    }),

    sparql.queryJson(predicateQuery, graphUri).then((predicateList) => {
      predicateList.forEach((result) => {
        result.uri = result.predicate
        delete result.predicate
        result.name = shortName(result.uri)
      })

      predicateList.sort(sortByNames)

      return Promise.resolve({ predicateList: predicateList })
    }),

    sparql.queryJson(typeQuery, graphUri).then((typeList) => {
      typeList.forEach((result) => {
        result.uri = result.object
        delete result.object
        result.name = result.uri.toString().replace(biopaxNS, '')
      })

      typeList.sort(sortByNames)

      return Promise.resolve({ typeList: typeList })
    }),

    sparql.queryJson(roleQuery, graphUri).then((roleList) => {
      roleList.forEach((result) => {
        result.uri = result.object
        delete result.object
        if (result.uri.toString().startsWith(soNS)) {
          var soTerm = result.uri.toString().replace(soNS, '')
          var sequenceOntology = require('../ontologies/sequence-ontology')
          result.name = sequenceOntology[soTerm].name
        } else if (result.uri.toString().startsWith(igemNS)) {
          result.name = 'igem:' + result.uri.toString().replace(igemNS + 'partType/', '')
        } else {
          result.name = result.uri.toString()
        }
      })

      roleList.sort(sortByNames)

      return Promise.resolve({ roleList: roleList })
    }),

    sparql.queryJson(collectionQuery, graphUri).then((collectionList) => {
      collectionList.forEach((result) => {
        result.uri = result.subject
        delete result.subject
        result.name = result.name ? result.name : result.displayId
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
