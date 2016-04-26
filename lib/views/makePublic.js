
var stack = require('../stack');

var sbolmeta = require('sbolmeta')

var pug = require('pug')

var base64 = require('../base64')

var getSBOL = require('../get-sbol')

module.exports = function(req, res) {

    // todo is it already public?

    var uri = base64.decode(req.params.designURI)

    getSBOL(null, uri, req.userStore, function(err, sbol, componentDefinition) {

        if(err) {

            res.status(500).send(err.toString())
            return

        }

        var meta = sbolmeta.summarizeComponentDefinition(componentDefinition)

        var locals = {
            meta: meta,
            entries: sbol.componentDefinitions.filter((entry) => {
                return entry !== componentDefinition
            }).map(sbolmeta.summarizeComponentDefinition)
        }
        
        res.send(pug.renderFile('templates/views/makePublic.jade', locals))
    })


};



