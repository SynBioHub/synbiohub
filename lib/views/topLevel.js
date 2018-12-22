var { getType } = require('../query/type')
var async = require('async')
var config = require('../config')
var collection = require('./collection')
var topLevelView = require('./topLevelView')
var pug = require('pug')
var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

    const { graphUri, uri, designId } = getUrisFromReq(req, res);

    getType(uri, graphUri).then((result) => {

        if(result==='http://sbols.org/v2#Collection') {
            collection(req, res)
            return
        } else {
            topLevelView(req, res, result)
            return
        }

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


