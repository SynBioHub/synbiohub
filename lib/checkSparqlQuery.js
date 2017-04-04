
const config = require('./config')

function checkQuery(query, user) {

    //const isAdmin = user && user.isAdmin

    const isAdmin = false

    checkNode(query)

    function checkNode(node) {

        if(node.graph !== undefined) {

            if(!isAdmin) {
                throw new Error('Graph cannot be specified inside query')
            }

        }

        switch(node.type) {

            case 'update':

                if(!isAdmin) {
                    throw new Error('Update queries are not allowed')
                }

                break

            case 'query':
                checkQueryNode(node)
                break

            case 'service':
                checkServiceNode(node)
                break

            case 'bgp':
                checkBgpNode(node)
                break

            case 'filter':
                break

            case 'optional':
                break

            default:
                throw new Error('unknown node type: ' + node.type)

        }

    }

    function checkQueryNode(node) {

        switch(node.queryType) {

            case 'SELECT':
                break

            default:
                throw new Error('unknown query type: ' + node.queryType)

        }

        node.where.forEach(checkNode)

    }

    function checkServiceNode(node) {

        const wor = Object.values(config.get('webOfRegistries'))

        if(!checkWor(node.name)) {
            throw new Error('Service ' + node.name + ' is not listed in the web of registries')
        }

        node.patterns.forEach(checkNode)

        function checkWor(url) {

            for(var i = 0; i < wor.length; ++ i)
                if(url.indexOf(wor[i]) === 0)
                    return true

            return false
        }
    }

    function checkBgpNode(node) {

    }
}

module.exports = checkQuery

