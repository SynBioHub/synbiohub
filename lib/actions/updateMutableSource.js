
const pug = require('pug')

const sparql = require('../sparql/sparql')

const loadTemplate = require('../loadTemplate')

const getUrisFromReq = require('../getUrisFromReq')

const config = require('../config')

const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

const wiky = require('../wiky/wiky')

module.exports = function(req, res) {

    const uri = req.body.uri
    const userUri = config.get('databasePrefix') + 'user/' + req.user.username

    const graphUri = getGraphUriFromTopLevelUri(uri,userUri)

    const source = req.body.source

    var sourceSparql = ''
    if (source.trim() != '') {
	sourceSparql = '<' + uri + '> sbh:mutableProvenance ' + JSON.stringify(source) + ' .'
    }

    var d = new Date();
    var modified = d.toISOString()
    modified = modified.substring(0,modified.indexOf('.'))

    const updateQuery = loadTemplate('./sparql/UpdateMutableSource.sparql', {
        topLevel: uri,
        source: sourceSparql,
	modified: JSON.stringify(modified)
    })

    sparql.query(updateQuery, graphUri).then((result) => {

        const locals = {
            config: config.get(),
            src: source,
            source: source!=''?wiky.process(source, {}):'',
            canEdit: true
        }

        res.send(pug.renderFile('templates/partials/mutable-source.jade', locals))
        
    })
}


