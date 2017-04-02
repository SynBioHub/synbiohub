
var { getVersion } = require('../query/version')

var async = require('async')

var config = require('../config')

var pug = require('pug')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

    const { graphUri, uri, designId, url } = getUrisFromReq(req)

    getVersion(uri, graphUri).then((result) => {
	
	res.redirect(url + '/' + result)

    }).catch((err) => {

        var locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err.stack ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
        
    })
	
};


