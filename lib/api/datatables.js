var sha1 = require('sha1')

var config = require('../config')

const getOwnedBy = require('../query/ownedBy')

const lookupRole = require('../role')

const {
  getCollectionMemberCount,
  getCollectionMembers
} = require('../query/collection')

const uriToUrl = require('../uriToUrl')

function datatables (req, res) {
  if (req.query.type === 'collectionMembers') {
    collectionMembersDatatable(req, res)
  } else {
    res.status(404).send('???')
  }
}

module.exports = datatables

function collectionMembersDatatable (req, res) {
  const uri = req.query.collectionUri

  var baseUri = uri.substring(0, uri.lastIndexOf('/'))
  baseUri = baseUri.substring(0, baseUri.lastIndexOf('/') + 1)

  const graphUri = req.query.graphUri ? req.query.graphUri : config.get('triplestore').defaultGraph
  // const graphUri = getGraphUriFromTopLevelUri(uri, req.user)

  const offset = parseInt(req.query.start)
  const limit = parseInt(req.query.length)

  const sortParams = req.query.order !== undefined && req.query.order.length === 1 ? req.query.order[0] : {}
  const typeFilter = req.query.typeFilter

  if (sortParams) {
    sortParams.column = [
      'name',
      'displayId',
      'type',
      'description'
    ][parseInt(sortParams['column'])]
  }

  const search = req.query.search.value

  Promise.all([

    getCollectionMemberCount(uri, graphUri),
    getCollectionMemberCount(uri, graphUri, search, typeFilter),
    getCollectionMembers(uri, graphUri, limit, offset, sortParams, search, typeFilter),
    getOwnedBy(uri, graphUri)

  ]).then((result) => {
    const [count, filterCount, members, ownedBy] = result

    let myOwnedBy = config.get('databasePrefix') + 'user/' + req.user.username

    const uniqueMembers = members.filter(
      (item, index, self) =>
        index === self.findIndex((t) => t.uri === item.uri)
    )

    res.header('content-type', 'application/json').send(JSON.stringify({
      draw: parseInt(req.query.draw),
      recordsTotal: count,
      recordsFiltered: filterCount,
      data: uniqueMembers.map((member) => {
        var memberUrl = uriToUrl(member.uri)
        if (member.uri.toString().startsWith(config.get('databasePrefix') + 'user/')) {
          if (req.headers.referer.toString().endsWith('/share')) {
            memberUrl += '/' + sha1('synbiohub_' + sha1(member.uri) + config.get('shareLinkSalt')) + '/share'
          }
        }

        var memberTypeUrl = member.type ? member.type : ''
        var memberType = member.type ? member.type.slice(member.type.lastIndexOf('#') + 1) : 'Unknown'
        if (member.sbolType) {
          memberTypeUrl = member.sbolType
          memberType = member.sbolType.slice(member.sbolType.lastIndexOf('#') + 1)
        }
        if (member.role) {
          memberTypeUrl = member.role
          memberType = lookupRole(member.role).description.name
        }
        if (memberType === 'ComponentDefinition') memberType = 'Component'
        else if (memberType === 'ModuleDefinition') memberType = 'Module'

        var persistentId = memberUrl.substring(0, memberUrl.lastIndexOf('/'))

        var memberId = member.displayId ? member.displayId : persistentId.slice(persistentId.lastIndexOf('/') + 1)

        const memberName = member.name ? member.name : memberId

        if (member.description) {
          member.description = member.description.length < 100 ? member.description : member.description.substring(0, 200) + '...'
        }

        var deleteString = ''
        if (!baseUri.toString().startsWith(config.get('databasePrefix') + 'public') ||
            (config.get('removePublicEnabled') && req.user.isAdmin && ownedBy.indexOf(myOwnedBy) !== -1)) {
          if (member.uri.toString().startsWith(baseUri)) {
            if (!member.uri.toString().startsWith(config.get('databasePrefix') + 'public') ||
                (config.get('removePublicEnabled') && req.user.isAdmin && ownedBy.indexOf(myOwnedBy) !== -1)) {
              deleteString = '<a class="delete" href="#" title="Remove this object from the repository"> <span class="fa fa-trash" /> </a>'
            }
          } else {
            deleteString = '<a class="remove" href="#" title="Remove this object from this collection"> <span class="fa fa-chain-broken" /> </a>'
          }
        }

        return [
          '<a href="' + memberUrl + '">' + memberName + '</a>',
          '<a href="' + memberUrl + '">' + memberId + '</a>',
          memberType + '&nbsp<a href="' + memberTypeUrl + '",title="Learn more about this type of record"><span class="fa fa-info-circle"></span> </a>',
          member.description,
          deleteString
        ]
      })
    }))
  }).catch((err) => {
    res.header('content-type', 'application/json').send(JSON.stringify({
      error: err.stack
    }))
  })
}
