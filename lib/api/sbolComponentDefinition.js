var pug = require('pug')

var getComponentDefinition = require('../../lib/get-sbol').getComponentDefinition

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)
	
    getComponentDefinition(uri, graphUris).then((res) => {
        
        const sbol = res.sbol
        const componentDefinition = res.object

        res.status(200)
           .type('application/rdf+xml')
           .send(serializeSBOL(sbol))
           //.set({ 'Content-Disposition': 'attachment; filename=' + componentDefinition.name + '.xml' })


    }).catch((err) => {

        var locals = {
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })
};


