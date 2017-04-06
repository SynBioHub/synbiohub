
const pug = require('pug')

const sparql = require('../sparql/sparql')

const loadTemplate = require('../loadTemplate')

const getUrisFromReq = require('../getUrisFromReq')

const config = require('../config')

const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

const wiky = require('../wiky/wiky')

const getOwnedBy = require('../query/ownedBy')

module.exports = function(req, res) {

    const uri = req.body.uri

    const graphUri = getGraphUriFromTopLevelUri(uri,req.user)

    const desc = req.body.value

    var descSparql = ''
    if (desc.trim() != '') {
	descSparql = '<' + uri + '> sbh:mutableDescription ' + JSON.stringify(desc) + ' .'
    }

    var d = new Date();
    var modified = d.toISOString()
    modified = modified.substring(0,modified.indexOf('.'))

    const updateQuery = loadTemplate('./sparql/UpdateMutableDescription.sparql', {
        topLevel: uri,
        desc: descSparql,
	modified: JSON.stringify(modified)
    })

    getOwnedBy(uri, graphUri).then((ownedBy) => {

        if(ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
            res.status(401).send('not authorized to edit this submission')
            return
        }

        return sparql.updateQuery(updateQuery, graphUri).then((result) => {

            const locals = {
                config: config.get(),
                src: desc,
                desc: desc!=''?wiky.process(desc, {}):'',
                canEdit: true
            }

            res.send(pug.renderFile('templates/partials/mutable-description.jade', locals))
            
        })

    })
}


