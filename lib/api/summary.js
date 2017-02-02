var pug = require('pug')

var getComponentDefinition = require('../../lib/get-sbol').getComponentDefinition

var sbolmeta = require('sbolmeta')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

exports = module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)
    
    getComponentDefinition(uri, stores).then((componentDefinition) => {

        if(!componentDefinition) {
            return res.status(404).send('not found\n' + uri)
        }

        try {
            res.status(200)
                .type('application/json')
                .send(JSON.stringify(sbolmeta.summarizeDocument(sbol), null, 2))
        }catch(e) {
            res.status(500).send(e.stack)
        }

    }).catch((err) => {

        const locals = {
            section: 'errors',
            user: req.user,
            errors: [ uri + ' Not Found' ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })

};


