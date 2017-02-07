
var pug = require('pug')

var getCollection = require('../../lib/get-sbol').getCollection

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)
	
    getCollection(uri, graphUris).then((result) => {
        
        const sbol = result.sbol
        const collection = result.object

        res.status(200)
           .type('application/rdf+xml')
           .send(serializeSBOL(sbol))
           //.set({ 'Content-Disposition': 'attachment; filename=' + collection.name + '.xml' })


    }).catch((err) => {
	if (req.url.endsWith('/sbol')) {
	    return res.status(404).send(uri + ' not found')
	} else { 
            var locals = {
		section: 'errors',
		user: req.user,
		errors: [ err ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
	}
    })
};


