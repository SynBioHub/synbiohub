var pug = require('pug')

var getSequence= require('../../lib/get-sbol').getSequence

var sbolmeta = require('sbolmeta')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)

    getSequence(uri, graphUris).then((sequence) => {

        var meta = sbolmeta.summarizeSequence(sequence)

        var lines = []
        var charsPerLine = 70

        lines.push('>' + meta.name 
            + ' (' + meta.length + ' ' + meta.lengthUnits + ')')

        for(var i = 0; i < meta.length; ) {

            lines.push(meta.elements.substr(i, charsPerLine))
            i += charsPerLine
        }

        var fasta = lines.join('\n')

        res.header('content-type', 'text/plain').send(fasta)

    }).catch((err) => {

        const locals = {
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }

        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })

};


