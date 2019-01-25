
const sparql = require('./sparql/sparql')

const ExecutionTimer = require('./util/execution-timer')

const TrieSearch = require('trie-search')

var autocompleteTitle = new TrieSearch('name', {
  splitOnRegEx: /\s|_/g
})

function updateCache () {
  const query = [
    'SELECT ?subject ?title WHERE {',
    '{',
    '?subject a <http://sbols.org/v2#ComponentDefinition> .',
    '?subject <http://purl.org/dc/terms/title> ?title .',
    '}',
    'UNION',
    '{',
    '?subject a <http://sbols.org/v2#ComponentDefinition> .',
    '?subject <http://sbols.org/v2#displayId> ?title .',
    '}',
    '}'
  ].join('\n')

  const queryTimer = ExecutionTimer('Retrieve list of subjects from triplestore')

  sparql.queryJson(query, null).then((results) => {
    titleToUri = []

    queryTimer()

    const populateTitleToUriTimer = ExecutionTimer('Populate title to URI list')

    results.forEach((result) => {
      autocompleteTitle.add({ name: result.title, uri: result.subject })
    })

    populateTitleToUriTimer()
  })
}

module.exports = {

  update: updateCache,
  autocompleteTitle: autocompleteTitle

}
