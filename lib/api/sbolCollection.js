var pug = require('pug')

var getCollection = require('../../lib/get-sbol').getCollection

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)

    getCollection(uri, stores, function(err, sbol, collection) {

        if(err) {

            locals = {
                section: 'errors',
                user: req.user,
                errors: [ err ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return        

        } else {

            res.status(200)
                .type('application/rdf+xml')
                .send(serializeSBOL(sbol))
            //.set({ 'Content-Disposition': 'attachment; filename=' + componentDefinition.name + '.xml' })

        }

    });

};


