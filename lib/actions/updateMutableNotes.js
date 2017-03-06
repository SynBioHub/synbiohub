
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

    const notes = req.body.notes

    var notesSparql = ''
    if (notesSparql.trim() != '') {
	notesSparql = '<' + uri + '> sbh:mutableNotes ' + JSON.stringify(notes) + ' .'
    }

    var d = new Date();
    var modified = d.toISOString()
    modified = modified.substring(0,modified.indexOf('.'))

    const updateQuery = loadTemplate('./sparql/UpdateMutableNotes.sparql', {
        topLevel: uri,
        notes: notesSparql,
	modified: JSON.stringify(modified)
    })

    sparql.query(updateQuery, graphUri).then((result) => {

        const locals = {
            config: config.get(),
            src: notes,
            notes: notes!=''?wiky.process(notes, {}):'',
            canEdit: true
        }

        res.send(pug.renderFile('templates/partials/mutable-notes.jade', locals))
        
    })
}


