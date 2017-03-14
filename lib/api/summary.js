var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var sbolmeta = require('sbolmeta')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

exports = module.exports = function(req, res) {

    const { graphUri, uri, designId, share } = getUrisFromReq(req)
    
    fetchSBOLObjectRecursive(uri, graphUri).then((result) => {

        const sbol = result.sbol
        const componentDefinition = result.object

        res.status(200)
            .type('application/json')
            .send(JSON.stringify(sbolmeta.summarizeDocument(sbol), null, 2))

    }).catch((err) => {

        const locals = {
            section: 'errors',
            user: req.user,
            errors: [ uri + ' Not Found' ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })

};


