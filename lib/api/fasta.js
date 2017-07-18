
const { getType } = require('../query/type')

var async = require('async')

var config = require('../config')

//var fastaCollection = require('./fastaCollection')

var fastaComponentDefinition = require('./fastaComponentDefinition')
var fastaCollection = require('./fastaCollection')

//var fastaModule = require('./fastaModule')

var fastaSequence = require('./fastaSequence')

var config = require('../config')

var pug = require('pug')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

    const { graphUri, uri, designId, share } = getUrisFromReq(req, res)

    getType(uri, graphUri).then((result) => {

        if(result && result==='http://sbols.org/v2#ComponentDefinition') { 
            fastaComponentDefinition(req, res)
            return
        } else if(result && result==='http://sbols.org/v2#Sequence') { 
            fastaSequence(req, res)
            return
        } else if(result && result==='http://sbols.org/v2#Collection') {
            fastaCollection(req, res)
            return
        }
        /*
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#ModuleDefinition') { 
            fastaModuleDefinition(req, res)
            return
                    } else if(result[0] && result[0].type==='http://sbols.org/v2#Model') { 
            fastaModel(req, res)
            return
                    } else }
            fastaGenericTopLevel(req, res)
            return
                    } */ else {
                        locals = {
                            config: config.get(),
                            section: 'errors',
                            user: req.user,
                            errors: [ uri + ' is a ' + result + '.', 
                                'FASTA conversion not supported for this type.' ]
                        }
                        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
                        return
                    }


    }).catch((err) => {

        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
        return        

    })

};
