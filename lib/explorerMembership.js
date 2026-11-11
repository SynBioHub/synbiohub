const request = require('request')
const config = require('./config')

// Tells SBOLExplorer that a part was added to or removed from a collection, so
// its search index (collections facet, cached Virtuoso results) follows the
// change without a full re-index. Fire and forget: SynBioHub has already
// written the change to Virtuoso, and a failure here must not fail the request.
module.exports = function notifyExplorerMembership (collectionUri, memberUri, action) {
  if (!config.get('useSBOLExplorer')) {
    return
  }

  request({
    method: 'POST',
    url: config.get('SBOLExplorerEndpoint') + 'incrementalmembership',
    json: { changes: [ { subject: memberUri, collection: collectionUri, action: action } ] }
  }, function (error, response, body) {
    if (error || response.statusCode >= 300) {
      console.error('SBOLExplorer incrementalmembership failed: ' + (error || body))
    }
  })
}
