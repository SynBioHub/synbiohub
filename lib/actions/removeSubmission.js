var async = require('async');

var request = require('request')

var loadTemplate = require('../loadTemplate')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

var sparql = require('../sparql/sparql')

module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)

    const graphUri = graphUris[0]

    var uriPrefix = uri.substring(0,uri.lastIndexOf('/'))
    uriPrefix = uriPrefix.substring(0,uriPrefix.lastIndexOf('/')+1)

    var templateParams = {
        uriPrefix: uriPrefix,
	version: req.params.version
    }

    var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

    sparql.queryJson(removeQuery, graphUri).then(() => {

        res.redirect('/manage');
        
    }).catch((err) => {

        res.status(500).send(err.stack)
                
    })
};


