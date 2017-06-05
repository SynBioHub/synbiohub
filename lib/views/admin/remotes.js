
const pug = require('pug')

const sparql = require('../../sparql/sparql')

const db = require('../../db')

const config = require('../../config')

const { getSnapshots } = require('../../snapshots')

module.exports = function(req, res) {

    const remotesConfig = config.get('remotes')

    const remotes = Object.keys(remotesConfig).map((id) => remotesConfig[id])

    remotes.forEach((remote) => {

        remote.rootCollectionUri = 'public/'
            + remote.id
            + '/'
            + remote.rootCollection.displayId
            + '/current'

        remote.rootCollectionUrl = '/' + remote.rootCollectionUri
    })

    function getRemoteSnapshots(remote) {

        return getSnapshots(remote.rootCollectionUri).then((snapshots) => {

            remote.snapshots = snapshots

        })
    }

    Promise.all(remotes.map(getRemoteSnapshots)).then(() => {

        var locals = {
            config: config.get(),
            section: 'admin',
            adminSection: 'remotes',
            user: req.user,
            remotes: remotes,
            remoteTypes: ['ice', 'benchling']
        }

        res.send(pug.renderFile('templates/views/admin/remotes.jade', locals))
    
    })

}

