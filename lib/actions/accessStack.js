
var config = require('../config')

module.exports = function(req, res) {

    if(req.method === 'POST') {

	console.log('POST: ' + config.get('backendURL') + req.url)
	return res.redirect(307,config.get('backendURL') + req.url);

    } else {

	console.log('GET: ' + config.get('backendURL') + req.url)
	return res.redirect(config.get('backendURL') + req.url);

    }

};


