var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

exports = module.exports = function (req, res) {
  const { graphUri, uri } = getUrisFromReq(req, res)

  fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
    const sbol = result.sbol

    res.header('Content-Disposition', 'attachment; filename="' + req.params.displayId + '.json"')
    res.header('Access-Control-Expose-Headers', 'Content-Disposition')
    res.status(200)
      .type('application/json')
      .send(sbol.serializeJSON({
        'xmlns:synbiohub': 'http://synbiohub.org#',
        'xmlns:sbh': 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#',
        'xmlns:sybio': 'http://www.sybio.ncl.ac.uk#',
        'xmlns:rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
        'xmlns:ncbi': 'http://www.ncbi.nlm.nih.gov#',
        'xmlns:igem': 'http://wiki.synbiohub.org/wiki/Terms/igem#',
        'xmlns:genbank': 'http://www.ncbi.nlm.nih.gov/genbank#',
        'xmlns:gbconv': 'http://sbols.org/genBankConversion#',
        'xmlns:dcterms': 'http://purl.org/dc/terms/',
        'xmlns:dc': 'http://purl.org/dc/elements/1.1/',
        'xmlns:obo': 'http://purl.obolibrary.org/obo/'
      }))
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
