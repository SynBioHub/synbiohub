var sha1 = require('sha1');

var config = require('../config')

const {
    getCollectionMemberCount,
    getCollectionMembers
} = require('../query/collection')
const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

const uriToUrl = require('../uriToUrl')

function datatables(req, res) {

    if (req.query.type === 'collectionMembers') {

        collectionMembersDatatable(req, res)

    } else {

        res.status(404).send('???')

    }


}

module.exports = datatables

function collectionMembersDatatable(req, res) {

    const uri = req.query.collectionUri

    const graphUri = req.query.graphUri ? req.query.graphUri : config.get('triplestore').defaultGraph
    //const graphUri = getGraphUriFromTopLevelUri(uri, req.user)

    const offset = parseInt(req.query.start)
    const limit = parseInt(req.query.length)

    const sortParams = req.query.order !== undefined && req.query.order.length === 1 ? req.query.order[0] : {}

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
        getCollectionMemberCount(uri, graphUri, search),
        getCollectionMembers(uri, graphUri, limit, offset, sortParams, search)

    ]).then((result) => {

        const [count, filterCount, members] = result

        res.header('content-type', 'application/json').send(JSON.stringify({
            draw: parseInt(req.query.draw),
            recordsTotal: count,
            recordsFiltered: filterCount,
            data: members.map((member) => {

                var memberUrl = uriToUrl(member.uri)
                if (member.uri.toString().startsWith(config.get('databasePrefix')+'user/')) {
                    if (req.headers.referer.toString().endsWith('/share')) {
                        memberUrl += '/' + sha1('synbiohub_' + sha1(member.uri) + config.get('shareLinkSalt')) + '/share'
                    }
                }

                var memberTypeUrl = member.type?member.type:'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Unknown'
                var memberType = member.type?member.type.slice(member.type.lastIndexOf('#') + 1):'Unknown'

                if (memberType === 'Collection') {
                    memberTypeUrl = 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Collection'
                    memberType = 'Collection'
                } else if (memberType === 'ComponentDefinition') {
                    memberTypeUrl = 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Part'
                    memberType = 'Component'
                } else if (memberType === 'ModuleDefinition') {
                    memberTypeUrl = 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Device'
                    memberType = 'Module'
                } else if (memberType === 'Sequence') {
                    memberTypeUrl = 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Sequence'
                    memberType = 'Sequence'
                } else if (memberType === 'Model') {
                    memberTypeUrl = 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Model'
                    memberType = 'Model'
                } else if (memberType === 'Attachment') {
                    memberTypeUrl = 'http://wiki.synbiohub.org/wiki/Terms/SynBioHub#Attachment'
                    memberType = 'Attachment'
                }

		var persistentId = memberUrl.substring(0,memberUrl.lastIndexOf('/'))
		
		var memberId = member.displayId?member.displayId:persistentId.slice(persistentId.lastIndexOf('/')+1)

                const memberName = member.name ? member.name : memberId

                if (member.description) {
                    member.description = member.description.length < 100 ? member.description : member.description.substring(0, 200) + '...'
                }

                return [
                    '<a href="' + memberUrl + '">' + memberName + '</a>',
                    '<a href="' + memberUrl + '">' + memberId + '</a>',
                    memberType + '&nbsp<a href="' + memberTypeUrl + '",title="Learn more about this type of record"><span class="fa fa-info-circle"></span> </a>',
                    member.description
                ]
            })
        }))

    }).catch((err) => {

        res.header('content-type', 'application/json').send(JSON.stringify({
            error: err.stack
        }))

    })
}
