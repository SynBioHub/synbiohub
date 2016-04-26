exports = module.exports = function(req, res) {

    // todo is it already public?

    var uri = base64.decode(req.params.designURI)

    getSBOL(null, uri, req.userStore, function(err, sbol, componentDefinition) {

        var meta = sbolmeta.summarizeComponentDefinition(componentDefinition)
        
        res.send(pug.renderFile('templates/views/makePublic.jade', locals))
    })


};


