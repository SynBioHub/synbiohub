
const pug = require('pug')

const sparql = require('../sparql/sparql')

const loadTemplate = require('../loadTemplate')

const getUrisFromReq = require('../getUrisFromReq')

const getComponentDefinition = require('../get-sbol').getComponentDefinition

const sbolmeta = require('sbolmeta')

const config = require('../config')

module.exports = function(req, res) {

    const uri = req.body.uri
    const userUri = config.get('databasePrefix') + 'user/' + req.user.username

    const graphUris = [
        userUri,
        null /* public */
    ]

    const desc = req.body.desc

    const updateQuery = loadTemplate('./sparql/UpdateMutableDescription.sparql', {
        topLevel: uri,
        desc: sparql.escape('"%L"', desc)
    })

    getComponentDefinition(uri, graphUris).then((res) => {

        console.log(res)

        const componentDefinition = res.object

        if(!req.user.isAdmin) {

            const ownedBy = componentDefinition.getAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')

            if(ownedBy !== userUri) {

                return Promise.reject(new Error('bad permissions'))

            }

        }

        return sparql.queryJson(updateQuery, componentDefinition.graphUri)
            
    }).then((res) => {

        const meta = sbolmeta.summarizeComponentDefinition(componentDefinition)

        const locals = {
            heading: meta.description,
            desc: desc
        }

        res.send(pug.renderFile('templates/partials/mutable-description.jade', locals))
    })
}


