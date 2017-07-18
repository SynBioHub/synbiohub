var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var sbolmeta = require('sbolmeta')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

    const { graphUri, uri, designId, share } = getUrisFromReq(req, res)

    fetchSBOLObjectRecursive(uri, graphUri).then((result) => {

        const sbol = result.sbol
        const componentDefinition = result.object

        var meta = sbolmeta.summarizeComponentDefinition(componentDefinition)

        var lines = []
        var charsPerLine = 70

        meta.sequences.forEach((sequence, i) => {

            lines.push('>' + meta.name + ' sequence ' + (i + 1)
                + ' (' + sequence.length + ' ' + sequence.lengthUnits + ')')

            for(var i = 0; i < sequence.length; ) {

                lines.push(sequence.elements.substr(i, charsPerLine))
                i += charsPerLine
            }

        })

        var fasta = lines.join('\n')

        res.header('content-type', 'text/plain').send(fasta)

    }).catch((err) => {

        var locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }

        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })
};


