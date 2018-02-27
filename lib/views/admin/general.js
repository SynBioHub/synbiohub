const pug = require('pug')

const config = require('../../config')

module.exports = function(req, res) {
    if(req.method === 'POST') {
        post(req, res)
    } else {
        form(req, res)
    }
}


function form(req, res) {

	const locals = {
        config: config.get(),
        section: 'admin',
        adminSection: 'general',
        user: req.user
    }
	
    res.send(pug.renderFile('templates/views/admin/general.jade', locals))
}

function post(req, res) {
    console.log(req.body)
    if(req.body.instanceUrl !== undefined && req.body.instanceUrl !== "") {
        config.set('instanceUrl', req.body.instanceUrl)
    }

    if(req.body.databasePrefix !== undefined && req.body.databasePrefix !== "") {
        config.set('databasePrefix', req.body.databasePrefix)
    }

    if(req.body.allowPublicSignup) {
        config.set('allowPublicSignup', true)
    } else {
        config.set('allowPublicSignup', false)
    }

    form(req, res);
}



