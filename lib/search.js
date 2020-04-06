var loadTemplate = require('./loadTemplate')

var escape = require('pg-escape')

var prefixify = require('./prefixify')

var config = require('./config')

const sparql = require('./sparql/sparql')
function getShortName (type) {
  let typeShortName = 'Unknown'
  let lastHash = type.lastIndexOf('#')
  let lastSlash = type.lastIndexOf('/')

  if (lastHash >= 0 && lastHash + 1 < type.length) {
    typeShortName = type.slice(lastHash + 1)

    switch (typeShortName) {
      case 'Component':
        typeShortName = 'ComponentInstance'
        break
      case 'Module':
        typeShortName = 'ModuleInstance'
        break
      case 'ComponentDefinition':
        typeShortName = 'Component'
        break
      case 'ModuleDefinition':
        typeShortName = 'Module'
        break
    }
  } else if (lastSlash >= 0 && lastSlash + 1 < type.length) {
    typeShortName = type.slice(lastSlash + 1)
  }

  return typeShortName
}

function search (graphUri, criteria, offset, limit, user, explorer = false) {
  var templateParams = {
    criteria: criteria,
    offset: offset !== undefined ? ' OFFSET ' + offset : '',
    limit: limit !== undefined ? ' LIMIT ' + limit : '',
    from: ''
  }
  if (user) {
    templateParams.from = 'FROM <' + config.get('databasePrefix') + 'public>' +
' FROM <' + user.graphUri + '>'
  }

  var countQuery = loadTemplate('sparql/searchCount.sparql', templateParams)
  var searchQuery = loadTemplate('sparql/search.sparql', templateParams)

  console.debug(countQuery)
  console.debug(searchQuery)

  return Promise.all([

    sparql.queryJson(countQuery, graphUri, explorer).then((result) => {
      if (result && result[0] && result[0].count !== undefined) {
        return Promise.resolve(result[0].count)
      } else {
        return Promise.reject(new Error('could not get result count'))
      }
    }),

    sparql.queryJson(searchQuery, graphUri, explorer).then((results) => {
      return Promise.resolve(results.map((result) => {
        result.uri = result.subject
        delete result.subject

        if (!result.name || result.name === '') {
          result.name = result.displayId
        }

        result.typeName = getShortName(result.type)

        var prefixedUri = prefixify(result.uri)
        if (result.uri.toString().startsWith(config.get('databasePrefix'))) {
          result.url = '/' + result.uri.toString().replace(config.get('databasePrefix'), '')
        } else {
          result.url = result.uri.toString()
        }

        if (result.uri.toString().startsWith(config.get('databasePrefix') + 'public/')) {
          result.triplestore = 'public'
        } else if (result.uri.toString().startsWith(config.get('databasePrefix') + 'user/')) {
          result.triplestore = 'private'
        } else {
          result.triplestore = 'remote'
        }
        result.prefix = prefixedUri.prefix

        return result
      }))
    })

  ]).then((res) => {
    return Promise.resolve({
      count: res[0],
      results: res[1]
    })
  }).catch((err) => {
    console.error('Virtuoso not responding search')
    console.error(err.stack)
    return Promise.resolve({ count: 0, results: [] })
  })
}

search.lucene = function lucene (value) {
  var criteriaStr = ''
  var values = value.split('&')
  if (value.indexOf('created') > -1) {
    criteriaStr += '   ?subject dcterms:created ?cdate . '
  }
  if (value.indexOf('modified') > -1) {
    criteriaStr += '   ?subject dcterms:modified ?mdate . '
  }
  var i
  for (i = 0; i < values.length - 1; i++) {
    var query = values[i].split('=')
    if (query[0].indexOf(':') > -1) {
      criteriaStr += '   ?subject ' + query[0] + ' ' + query[1] + ' . '
    } else if (query[0] === 'objectType') {
      if (query[1].includes(':')) {
        criteriaStr += '   ?subject a ' + query[1] + ' . '
      } else {
        criteriaStr += '   ?subject a sbol2:' + query[1] + ' . '
      }
    } else if (query[0] === 'collection') {
      criteriaStr += '   ?collection a sbol2:Collection .' +
      '   ' + query[1] + ' sbol2:member ?subject .'
    } else if (query[0] === 'createdBefore') {
      criteriaStr += '   FILTER (xsd:dateTime(?cdate) <= "' + query[1] + 'T23:59:59Z"^^xsd:dateTime) '
    } else if (query[0] === 'createdAfter') {
      criteriaStr += '   FILTER (xsd:dateTime(?cdate) >= "' + query[1] + 'T00:00:00Z"^^xsd:dateTime) '
    } else if (query[0] === 'modifiedBefore') {
      criteriaStr += '   FILTER (xsd:dateTime(?mdate) <= "' + query[1] + 'T23:59:59Z"^^xsd:dateTime) '
    } else if (query[0] === 'modifiedAfter') {
      criteriaStr += '   FILTER (xsd:dateTime(?mdate) >= "' + query[1] + 'T00:00:00Z"^^xsd:dateTime) '
    } else if (query[0] === 'globalsequence') {
      return `?subject sbol2:sequence ?seq .\n    ?seq sbol2:elements "${query[1]}" .`
    } else if (query[0] === 'sequence') {
      return `?subject sbol2:sequence ?seq .\n    ?seq sbol2:elements "EXACT${query[1]}" .`
    } else {
      criteriaStr += '   ?subject sbol2:' + query[0] + ' ' + query[1] + ' . '
    }
  }
  if (values[values.length - 1] !== '') {
    var searchTerms = values[values.length - 1].split(/[ ]+/)
    criteriaStr += 'FILTER ('
    var andMode = true
    var notMode = false
    for (i = 0; i < searchTerms.length; i++) {
      if (searchTerms[i] === 'and') {
        andMode = true
        continue
      } else if (searchTerms[i] === 'or') {
        andMode = false
        continue
      } else if (searchTerms[i] === 'not') {
        notMode = true
        continue
      }
      if (i > 0) {
        if (andMode) {
          criteriaStr += '&&'
          andMode = false
        } else {
          criteriaStr += '||'
        }
      }
      if (notMode) {
        criteriaStr += ' !'
      }
      criteriaStr += escape(
        '(CONTAINS(lcase(?displayId), lcase(%L))||CONTAINS(lcase(?name), lcase(%L))||CONTAINS(lcase(?description), lcase(%L)))',
        searchTerms[i], searchTerms[i], searchTerms[i]
      ).replace(/''/g, '\\\'')
    }
    criteriaStr += ')'
  }

  return criteriaStr
}

module.exports = search
