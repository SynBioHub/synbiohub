const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')
const { getComponentDefinitionMetadata } = require('../query/component-definition')
const { getContainingCollections } = require('../query/local/collection')
const loadTemplate = require('../loadTemplate')
const async = require('async')
const prefixify = require('../prefixify')
const pug = require('pug')
const sparql = require('../sparql/sparql-collate')
const getDisplayList = require('visbol/lib/getDisplayList').getDisplayList
const config = require('../config')
const URI = require('sboljs').URI
const getUrisFromReq = require('../getUrisFromReq')

module.exports = function (req, res) {

    var locals = {
        config: config.get(),
        section: 'component',
        user: req.user
    }

    const {
        graphUri,
        uri,
        designId,
        share,
        url
    } = getUrisFromReq(req, res)

    var templateParams = {
        uri: uri
    }

    fetchSBOLObjectRecursive('ComponentDefinition', uri, graphUri).then((result) => {

        sbol = result.sbol
        componentDefinition = result.object

        return componentDefinition;

    }).then(componentDefinition => {

        locals.meta = {
            displayList: getDisplayList(componentDefinition, config, req.url.toString().endsWith('/share'))
        }

        res.send(pug.renderFile('templates/views/visualization.jade', locals))

    }).catch((err) => {

        const locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [err.stack]
        }

        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })

};
