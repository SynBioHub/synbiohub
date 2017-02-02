var pug = require('pug')

var getSequence = require('../../lib/get-sbol').getSequence

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)
	
    getSequence(uri, graphUris).then((result) => {

        const sbol = result.sbol

        res.status(200)
            .type('application/rdf+xml')
            .send(serializeSBOL(sbol))
        //.set({ 'Content-Disposition': 'attachment; filename=' + componentDefinition.name + '.xml' })


    }).catch((err) => {

        const locals = {
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })


};


