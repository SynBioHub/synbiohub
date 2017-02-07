var getType = require('../get-sbol').getType

var async = require('async')

var config = require('../config')

var sbolCollection = require('./sbolCollection')

var sbolComponentDefinition = require('./sbolComponentDefinition')

var sbolModuleDefinition = require('./sbolModuleDefinition')

var sbolSequence = require('./sbolSequence')

var sbolModel = require('./sbolModel')

var sbolGenericTopLevel = require('./sbolGenericTopLevel')

var config = require('../config')

var pug = require('pug')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)

    getType(uri, graphUris).then((result) => {

        if(result[0] && result[0].type==='http://sbols.org/v2#Collection') {
            sbolCollection(req, res)
            return
        } else if(result[0] && result[0].type==='http://sbols.org/v2#ComponentDefinition') { 
            sbolComponentDefinition(req, res)
            return
        } else if(result[0] && result[0].type==='http://sbols.org/v2#ModuleDefinition') { 
            sbolModuleDefinition(req, res)
            return
        } else if(result[0] && result[0].type==='http://sbols.org/v2#Sequence') { 
            sbolSequence(req, res)
            return
        } else if(result[0] && result[0].type==='http://sbols.org/v2#Model') { 
            sbolModel(req, res)
            return
        } else {
            sbolGenericTopLevel(req, res)
            return
        }

    }).catch((err) => {
	
	if (req.url.endsWith('/sbol')) {
	    return res.status(404).send(uri + ' not found')
	} else { 
            locals = {
		section: 'errors',
		user: req.user,
		errors: [ err ]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
            return   
	}     
                
    })
	
};


