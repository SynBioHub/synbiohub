
var config = require('../config')

const  { getCollectionMemberCount, getCollectionMembers } = require('../query/collection')
const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

function datatables(req, res) {

    if(req.query.type === 'collectionMembers') {

        collectionMembersDatatable(req, res)

    } else {

        res.status(404).send('???')

    }


}

module.exports = datatables

function collectionMembersDatatable(req, res) {

    const uri = req.query.collectionUri
    const graphUri = getGraphUriFromTopLevelUri(uri)

    const offset = parseInt(req.query.start)
    const limit = parseInt(req.query.length)

    Promise.all([

        getCollectionMemberCount(uri, graphUri),
        getCollectionMembers(uri, graphUri, limit, offset)

    ]).then((result) => {

        const [ count, members ] = result

        res.header('content-type', 'application/json').send(JSON.stringify({
            draw: parseInt(req.query.draw),
            recordsTotal: count,
            recordsFiltered: count,
            data: members.map((member) => {

                const typeLocalPart = member.type.slice(member.type.lastIndexOf('#') + 1)

		if (member.uri.toString().startsWith(config.get('databasePrefix'))) {
		    memberUrl = '/' + member.uri.toString().replace(config.get('databasePrefix'),'')
		    if (req.url.toString().endsWith('/share')) {
			memberUrl += '/' + sha1('synbiohub_' + sha1(member.uri) + config.get('shareLinkSalt')) + '/share'
		    }
		} else {
		    memberUrl = member.uri
		}

                return [
                    '<a href="' + member.type + '">' + typeLocalPart + '</a>',
                    '<a href="' + memberUrl + '">' + member.displayId + '</a>',
                    '<a href="' + memberUrl + '">' + member.name + '</a>',
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



