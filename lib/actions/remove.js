var async = require('async');

var request = require('request')

var loadTemplate = require('../loadTemplate')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

var sparql = require('../sparql/sparql')

const getOwnedBy = require('../query/ownedBy')

const pug = require('pug');

module.exports = function(req, res) {

    req.setTimeout(0) // no timeout

    const { graphUri, uri, designId } = getUrisFromReq(req, res)

    var uriPrefix = uri.substring(0,uri.lastIndexOf('/')+1)

    var templateParams = {
        uriPrefix: uriPrefix,
	version: req.params.version
    }

    var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

    return getOwnedBy(uri, graphUri).then((ownedBy) => {

        if(ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
            //res.status(401).send('not authorized to edit this submission')
	    const locals = {
		config: config.get(),
		section: 'errors',
		user: req.user,
		errors: [ 'Not authorized to remove this submission' ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))        
        }


	sparql.deleteStaggered(removeQuery, graphUri).then(() => {

	    var templateParams = {
		uri: uri,
	    }

	    removeQuery = loadTemplate('sparql/removeReferences.sparql', templateParams)

	    sparql.deleteStaggered(removeQuery, graphUri).then(() => {

		res.redirect('/manage');

	    })
        
	}).catch((err) => {

            res.status(500).send(err.stack)
                
	})
    })
};


