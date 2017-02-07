var pug = require('pug')

var getGenericTopLevel = require('../../lib/get-sbol').getGenericTopLevel

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)

    getGenericTopLevel(uri, graphUris).then((result) => {

        const sbol = result.sbol

        res.status(200)
            .type('application/rdf+xml')
            .send(serializeSBOL(sbol))
            //.set({ 'Content-Disposition': 'attachment; filename=' + componentDefinition.name + '.xml' })

    }).catch((err) => {
	if (req.url.endsWith('/sbol')) {
	    return res.status(404).send(uri + ' not found')
	} else { 
            const locals = {
		section: 'errors',
		user: req.user,
		errors: [ err ] 
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
	}
    })

}


