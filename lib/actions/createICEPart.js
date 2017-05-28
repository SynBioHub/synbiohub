var pug = require('pug')

var extend = require('xtend')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

const ice = require('../ice')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

exports = module.exports = function(req, res) {
	
    var iceRemote = config.get("defaultICEInstance")

    if (!iceRemote) {
	if(req.method === 'GET') {

	    var iceRemotes = []
	    Object.keys(config.get('remotes')).filter(function(e){return config.get('remotes')[e].type ==='ice'}).forEach((remote) => {
		iceRemotes.push(config.get('remotes')[remote])
	    })
	
	    if (iceRemotes.length > 1) {
		locals = {
		    config: config.get(),
		    section: 'createICEPart',
		    user: req.user,
		    iceRemotes: iceRemotes,
		    submission: {}
		}
		res.send(pug.renderFile('templates/views/selectICERemote.jade', locals))
		return
	    }

	    if (iceRemotes.length === 0) {
		const locals = {
		    config: config.get(),
		    section: 'errors',
		    user: req.user,
		    errors: [ 'No ICE remote instances configured' ]
		}
		res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
		return
	    }
	    
	    if (iceRemotes.length === 1) {
		iceRemote = iceRemotes[0].id
	    }

	} else {
	    iceRemote = req.body.iceRemote
	}
    }
    const { graphUri, uri, designId, share } = getUrisFromReq(req)
    
    fetchSBOLObjectRecursive(uri, graphUri).then((result) => {

        const sbol = result.sbol
        const componentDefinition = result.object

	const remoteConfig = config.get('remotes')[iceRemote]
	
	return ice.createSequence(remoteConfig, sbol).then((result) => {
	    res.redirect('/public/' + remoteConfig.id + '/available/current')
	})

    }).catch((err) => {

        const locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ 'Problem sending part to ' + config.get('remotes')[iceRemote].rootCollection.name + ' ' + err]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })

};


