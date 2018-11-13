
var pug = require('pug')

var config = require('../config')

var generateDataCatalog = require('../bioschemas/DataCatalog')

module.exports = async function(req, res) {

	var locals = {
        config: config.get(),
        section: 'index',
        user: req.user,
        metaDesc: 'A parts repository for synthetic biology.',
				bioschemas: await generateDataCatalog()
    }
    res.send(pug.renderFile('templates/views/index.jade', locals))
};
