var { getType } = require('../query/type')
var async = require('async')
var config = require('../config')
var collection = require('./collection')
var componentDefinition = require('./componentDefinition')
var moduleDefinition = require('./moduleDefinition')
var sequence = require('./sequence')
var model = require('./model')
var attachment = require('./attachment')
var sbolAttachment = require('./sbolAttachment')
var genericTopLevel = require('./genericTopLevel')
var topLevelView = require('./topLevelView')
var activity = require('./activity')
var agent = require('./agent')
var plan = require('./plan')
var implementation = require('./implementation')
var test = require('./test')
var pug = require('pug')
var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

    const { graphUri, uri, designId } = getUrisFromReq(req, res);

    getType(uri, graphUri).then((result) => {

        if(result==='http://sbols.org/v2#Collection') {
            collection(req, res)
            return
        } else if(result==='http://sbols.org/v2#ComponentDefinition') { 
            topLevelView(req, res, result)
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


