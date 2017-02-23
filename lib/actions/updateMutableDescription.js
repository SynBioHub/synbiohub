
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

    const desc = req.body.desc

    const updateQuery = loadTemplate('./sparql/UpdateMutableDescription.sparql', {
        topLevel: uri,
        desc: JSON.stringify(desc)
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
            heading: meta.description,
            src: desc,
            desc: wiky.process(desc, {})
        }

        res.send(pug.renderFile('templates/partials/mutable-description.jade', locals))
    })
}


