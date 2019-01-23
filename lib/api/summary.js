var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

exports = module.exports = function (req, res) {
  const { graphUri, uri, designId, share } = getUrisFromReq(req, res)

  fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
    const sbol = result.sbol
    const componentDefinition = result.object

    res.status(200)
      .type('application/json')
      .send(sbol.serializeJSON({
        'xmlns:synbiohub': 'http://synbiohub.org#',
        'xmlns:sybio': 'http://www.sybio.ncl.ac.uk#',
        'xmlns:rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
        'xmlns:ncbi': 'http://www.ncbi.nlm.nih.gov#',
        'xmlns:igem': 'http://synbiohub.org/terms/igem/',
        'xmlns:genbank': 'http://www.ncbi.nlm.nih.gov/genbank/',
        'xmlns:annot': 'http://myannotation.org/',
        'xmlns:igem': 'http://parts.igem.org/#',
        'xmlns:pr': 'http://partsregistry.org/',
        'xmlns:grn': 'urn:bbn.com:tasbe:grn/',
        'xmlns:myapp': 'http://www.myapp.org/',
        'xmlns:sbolhub': 'http://sbolhub.org/',
        'xmlns:grn': 'urn:bbn.com:tasbe:grn/'
      }))
  }).catch((err) => {
    const locals = {
      config: config.get(),
      section: 'errors',
      user: req.user,
      errors: [ uri + ' Not Found' ]
    }
    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
  })
}
