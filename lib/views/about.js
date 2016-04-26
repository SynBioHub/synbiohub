
var pug = require('pug')

module.exports = function(req, res) {

	var locals = {
        section: 'about',
        user: req.user
    }
	
    res.send(pug.renderFile('templates/views/about.jade', locals))
	
};
