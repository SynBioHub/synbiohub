var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var sbolmeta = require('sbolmeta')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

    const { graphUri, uri, designId, share } = getUrisFromReq(req)

    fetchSBOLObjectRecursive(uri, graphUri).then((result) => {

        const sbol = result.sbol
        const sequence = result.object

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


