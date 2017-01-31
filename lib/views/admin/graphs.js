
var pug = require('pug')

module.exports = function(req, res) {

	var locals = {
        section: 'admin',
        adminSection: 'graphs',
        user: req.user
    }
	
    res.send(pug.renderFile('templates/views/admin/graphs.jade', locals))
	
};
