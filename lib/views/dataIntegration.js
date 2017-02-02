
var getComponentDefinition = require('../get-sbol').getComponentDefinition
var getContainingCollections = require('../get-sbol').getContainingCollections

var sbolmeta = require('sbolmeta')

var async = require('async')

var pug = require('pug')

var sparql = require('../sparql/sparql-collate')

var wiky = require('../wiky/wiky.js');

var config = require('../config')

var URI = require('urijs')

var getUrisFromReq = require('../getUrisFromReq')

const integrations = require('../integration/index')

module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)

    const graphUri = graphUris[0]

    getComponentDefinition(uri, graphUris).then((result) => {

        const locals = {
            section: 'dataIntegration',
            user: req.user,
            integrations: integrations
        }

        res.send(pug.renderFile('templates/views/dataIntegration.jade', locals))
    })
}


