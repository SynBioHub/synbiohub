
var pug = require('pug')

module.exports = function(req, res) {

	var locals = {
        section: 'index',
        user: req.user,
        metaDesc: 'A parts repository for synthetic biology.'
    }
    res.send(pug.renderFile('templates/views/index.jade', locals))
};


