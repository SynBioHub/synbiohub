
var pug = require('pug')

module.exports = function(req, res) {

	var locals = {
        section: 'jobs',
        user: req.user
    }
	
    res.send(pug.renderFile('templates/views/jobs.jade', locals))
	
};
