
var pug = require('pug')

const { fetchSBOLSource } = require('../fetch/fetch-sbol-source')

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

const fs = require('mz/fs')

module.exports = function(req, res) {

    const { graphUri, uri, designId, share } = getUrisFromReq(req)
	
    fetchSBOLSource(uri, graphUri).then((tempFilename) => {

        res.status(200).type('application/rdf+xml')
           //.set({ 'Content-Disposition': 'attachment; filename=' + collection.name + '.xml' })

        const readStream = fs.createReadStream(tempFilename)
            
        readStream.pipe(res)


    }).catch((err) => {
	if (req.url.endsWith('/sbol')) {
	    return res.status(404).send(uri + ' not found')
	} else { 
        var locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err.stack ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
	}
    })
};


