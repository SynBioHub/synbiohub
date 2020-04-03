
var pug = require('pug')

var Sboljs = require('sboljs')

var search = require('../search')

var extend = require('xtend')

var escape = require('pg-escape')

var config = require('../config')

var sparql = require('../sparql/sparql')

const serializeSBOL = require('../serializeSBOL')

var loadTemplate = require('../loadTemplate')

var namespace = require('../summarize/namespace')

module.exports = function (req, res) {
  if (req.method === 'POST') {
    if (req.body.adv.startsWith('Filter')) {
      advancedSearchForm(req, res, {}, [])
    } else {
      if (req.originalUrl.endsWith('/createCollection')) {
        var errors = checkCollectionMeta(req, res)
        if (errors.length > 0) {
          advancedSearchForm(req, res, {}, errors)
        } else {
          advancedSearchPost(req, res)
        }
      } else {
        advancedSearchPost(req, res)
      }
    }
  } else {
    advancedSearchForm(req, res, {}, [])
  }
}

function checkCollectionMeta (req, res) {
  var errors = []
  if (req.body.metaId === '') {
    errors.push('Please enter an id for your collection')
  }
  const idRegEx = new RegExp('^[a-zA-Z_]+[a-zA-Z0-9_]*$')
  if (!idRegEx.test(req.body.metaId)) {
    errors.push('Collection id is invalid. An id is a string of characters that MUST be composed of only alphanumeric or underscore characters and MUST NOT begin with a digit.')
  }
  if (req.body.metaVersion === '') {
    errors.push('Please enter a version for your collection')
  }
  const versionRegEx = /^[0-9]+[a-zA-Z0-9_\\.-]*$/
  if (!versionRegEx.test(req.body.metaVersion)) {
    errors.push('Version is invalid. A version is a string of characters that MUST be composed of only alphanumeric characters, underscores, hyphens, or periods and MUST begin with a digit.')
  }
  if (req.body.metaName === '') {
    errors.push('Please enter a name for your collection')
  }
  if (req.body.metaDescription === '') {
    errors.push('Please enter a brief description for your collection')
  }
  return errors
}

function advancedSearchForm (req, res, locals, errors) {
  locals = extend({
    config: config.get(),
    section: 'advancedSearch',
    user: req.user,
    errors: errors
  }, locals)

  getUriList(req, null).then((lists) => {
    var collectionMeta = { id: req.body.metaId ? req.body.metaId : '',
      version: req.body.metaVersion ? req.body.metaVersion : '',
      name: req.body.metaName ? req.body.metaName : '',
      description: req.body.metaDescription ? req.body.metaDescription : ''
    }

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

function getQuery (searchPred, searchObj, filterObj, criteriaStr, queryStr, list) {
  if (searchPred !== 'No Filter' && (searchObj || (filterObj && filterObj !== 'No Filter'))) {
    var predStr = searchPred
    if (!searchPred.toString().startsWith('sbol2:')) {
      predStr = searchPred.replace('sbol2:', '')
    }
    if (searchPred === 'a') {
      predStr = 'objectType'
    }
    var objStr
    if (searchObj) {
      objStr = namespace.longName(searchObj)
    } else {
      var mappedList = {}
      for (var i = 0; i < list.length; i++) {
        mappedList[list[i].name] = list[i]
      }
      objStr = mappedList[filterObj].uri
    }
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
    var criteria = []
    var query = getQuery('a', null, req.body.objectType, '', '', lists.typesList)
    query = getQuery('dc:creator', null, req.body.creator, query.criteriaStr, query.queryStr, lists.creatorList)
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
    query = getQuery(req.body.searchPred1, req.body.searchObj1, req.body.filterObj1, query.criteriaStr, query.queryStr, lists.object1List)
    query = getQuery(req.body.searchPred2, req.body.searchObj2, req.body.filterObj2, query.criteriaStr, query.queryStr, lists.object2List)
    query = getQuery(req.body.searchPred3, req.body.searchObj3, req.body.filterObj3, query.criteriaStr, query.queryStr, lists.object3List)
    query = getQuery(req.body.searchPred4, req.body.searchObj4, req.body.filterObj4, query.criteriaStr, query.queryStr, lists.object4List)
    query = getQuery(req.body.searchPred5, req.body.searchObj5, req.body.filterObj5, query.criteriaStr, query.queryStr, lists.object5List)
    query = getQuery('sbol2:type', null, req.body.type, query.criteriaStr, query.queryStr, lists.typeList)
    query = getQuery('sbol2:role', null, req.body.role, query.criteriaStr, query.queryStr, lists.roleList)
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
            result.name = namespace.shortName(result.uri)
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
            result.name = namespace.shortName(result.uri)
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
            result.name = namespace.shortName(result.uri)
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
            result.name = namespace.shortName(result.uri)
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
            result.name = namespace.shortName(result.uri)
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
        result.name = namespace.shortName(result.uri)
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
        result.name = namespace.shortName(result.uri)
      })

      predicateList.sort(sortByNames)

      return Promise.resolve({ predicateList: predicateList })
    }),

    sparql.queryJson(typeQuery, graphUri).then((typeList) => {
      typeList.forEach((result) => {
        result.uri = result.object
        delete result.object
        result.name = namespace.shortName(result.uri)
      })

      typeList.sort(sortByNames)

      return Promise.resolve({ typeList: typeList })
    }),

    sparql.queryJson(roleQuery, graphUri).then((roleList) => {
      roleList.forEach((result) => {
        result.uri = result.object
        delete result.object
        result.name = namespace.shortName(result.uri)
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
