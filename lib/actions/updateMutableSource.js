
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

    const source = req.body.source

    var d = new Date();
    var modified = d.toISOString()
    modified = modified.substring(0,modified.indexOf('.'))

    const updateQuery = loadTemplate('./sparql/UpdateMutableSource.sparql', {
        topLevel: uri,
        source: JSON.stringify(source),
	modified: JSON.stringify(modified)
    })

    var componentDefinition

    getComponentDefinition(uri, [ graphUri ]).then((res) => {

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
            src: source,
            source: wiky.process(source, {})
        }

        res.send(pug.renderFile('templates/partials/mutable-source.jade', locals))
    })
}


