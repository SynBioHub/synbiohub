var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var summarizeComponentDefinition = require('../summarize/summarizeComponentDefinition')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function (req, res) {
  const { graphUri, uri } = getUrisFromReq(req, res)

  fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
    const componentDefinition = result.object

    var meta = summarizeComponentDefinition(componentDefinition, req)

    var lines = []
    var charsPerLine = 70

    meta.sequences.forEach((sequence, i) => {
      lines.push('>' + meta.name + ' sequence ' + (i + 1) +
' (' + sequence.length + ' ' + sequence.lengthUnits + ')')

      for (i = 0; i < sequence.length;) {
        lines.push(sequence.elements.substr(i, charsPerLine))
        i += charsPerLine
      }
    })

    var fasta = lines.join('\n')
    res.status(200)
    res.header('Content-Disposition', 'attachment; filename="' + req.params.displayId + '.fasta"')
    res.header('content-type', 'text/plain').send(fasta)
  }).catch((err) => {
    if (!req.accepts('text/html')) {
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
}
