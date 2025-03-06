var uriToUrl = require('./uriToUrl')

function uriToMeta (uri, req) {
  var persId
  var version
  var id
  if (uri.toString().lastIndexOf('/')) {
    version = uri.toString().slice(uri.toString().lastIndexOf('/') + 1)
    if (version.match(/^\d/)) {
      persId = uri.toString().substring(0, uri.toString().lastIndexOf('/'))
    } else {
      version = ''
      persId = uri.toString()
    }
    id = persId.toString().slice(persId.toString().lastIndexOf('/') + 1)
  }

  return {
    uri: uri + '',
    id: id,
    version: version,
    name: id,
    url: uriToUrl(uri, req)
  }
}

module.exports = uriToMeta
