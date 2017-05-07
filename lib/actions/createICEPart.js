var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

const ice = require('../ice')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

exports = module.exports = function(req, res) {

    const { graphUri, uri, designId, share } = getUrisFromReq(req)
    
    fetchSBOLObjectRecursive(uri, graphUri).then((result) => {

        const sbol = result.sbol
        const componentDefinition = result.object

	const remoteConfig = config.get('remotes')[config.get("defaultICEInstance")]
	
	return ice.createSequence(remoteConfig, sbol).then((result) => {
	    console.log(JSON.stringify(result))
	    res.redirect('/public/' + remoteConfig.id + '/available/current')
	})

    }).catch((err) => {

	console.log(err)
        const locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ uri + ' Not Found' ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })

};


