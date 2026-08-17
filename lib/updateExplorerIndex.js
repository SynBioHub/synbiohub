const config = require('./config')
const request = require('request')
const loadTemplate = require('./loadTemplate')
const sparql = require('./sparql/sparql')

/* After a collection is submitted or made public, SynBioHub uploads its RDF
 * straight into the triplestore. SBOLExplorer's Elasticsearch index does not
 * see those parts until a full reindex, so a freshly uploaded part is missing
 * from search (issue #159). This asks SBOLExplorer to incrementally index the
 * collection and each of its members via GET /update?subject=<uri>, which
 * re-reads each subject from the triplestore and indexes it immediately.
 *
 * Fire-and-forget: failures are logged but never block or fail the submission,
 * and the whole thing is a no-op when useSBOLExplorer is disabled. */
function updateExplorerIndex (collectionUri, graphUri) {
  if (!config.get('useSBOLExplorer')) {
    return Promise.resolve()
  }

  // Enumerate members across BOTH the public graph and the (private) graph the
  // collection was uploaded to, so this works whether the collection is private
  // (submit) or public (make-public), and does not miss cross-graph members.
  const publicGraph = config.get('triplestore').defaultGraph
  const graphs = [publicGraph]
  if (graphUri && graphUri !== publicGraph) {
    graphs.push(graphUri)
  }
  const from = graphs.map((g) => 'FROM <' + g + '>').join('\n')

  const membersQuery = loadTemplate('sparql/getCollectionMemberUris.sparql', {
    from: from,
    collection: collectionUri
  })

  return sparql.queryJson(membersQuery, graphUri).then((members) => {
    const subjects = [collectionUri].concat(
      (members || []).map((m) => m.uri).filter(Boolean)
    )

    subjects.forEach((subject) => {
      request({
        method: 'GET',
        url: config.get('SBOLExplorerEndpoint') + 'update',
        qs: { subject: subject }
      }, (error, response, body) => {
        if (error) {
          console.log('SBOLExplorer index update failed for ' + subject + ': ' + error)
        }
      })
    })
  }).catch((err) => {
    console.log('SBOLExplorer index update error: ' + err)
  })
}

module.exports = updateExplorerIndex
