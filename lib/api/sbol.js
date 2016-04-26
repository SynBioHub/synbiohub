
var stack = require('../../lib/stack');

var getSBOL = require('../../lib/get-sbol')

module.exports = function(req, res) {

    stack.getPrefixes((err, prefixes) => {

        var baseUri
        var uri

        if(req.params.designURI) {
            uri = base64.decode(req.params.designURI)
        } else {
            baseUri = prefixes[req.params.prefix]
            uri = baseUri + req.params.designid
        }

        getSBOL(null, uri, req.userStore, function(err, sbol, componentDefinition) {

            if(err) {

                res.status(500).send(err)

            } else {

                res.status(200)
                   .type('application/rdf+xml')
                   .send(sbol.serializeXML({
                       'xmlns:sybio': 'http://www.sybio.ncl.ac.uk#',
                       'xmlns:rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
                       'xmlns:ncbi': 'http://www.ncbi.nlm.nih.gov#'
                   }))
                   //.set({ 'Content-Disposition': 'attachment; filename=' + componentDefinition.name + '.xml' })

            }

        });

    })

};


