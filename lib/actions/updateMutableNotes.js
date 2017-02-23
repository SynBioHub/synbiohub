
const pug = require('pug')

const sparql = require('../sparql/sparql')

const loadTemplate = require('../loadTemplate')

const getUrisFromReq = require('../getUrisFromReq')

const getComponentDefinition = require('../get-sbol').getComponentDefinition

const sbolmeta = require('sbolmeta')

const config = require('../config')

const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

const wiky = require('../wiky/wiky')

module.exports = function(req, res) {

    const uri = req.body.uri
    const userUri = config.get('databasePrefix') + 'user/' + req.user.username

    const graphUri = getGraphUriFromTopLevelUri(uri)

    const notes = req.body.notes

    const updateQuery = loadTemplate('./sparql/UpdateMutableNotes.sparql', {
        topLevel: uri,
        notes: JSON.stringify(notes)
    })

    var componentDefinition

    getComponentDefinition(uri, [ graphUri ]).then((res) => {

        console.log(res)

        componentDefinition = res.object

        if(!req.user.isAdmin) {

            const ownedBy = componentDefinition.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')

            if(ownedBy !== userUri) {

                return Promise.reject(new Error('bad permissions'))

            }

        }

        return sparql.queryJson(updateQuery, graphUri)
            
    }).then((result) => {

        const meta = sbolmeta.summarizeComponentDefinition(componentDefinition)

        const locals = {
            src: notes,
            notes: wiky.process(notes, {})
        }

        res.send(pug.renderFile('templates/partials/mutable-notes.jade', locals))
    })
}


