
var config = require('../config')

module.exports = function(req, res) {

    console.log(config.get('backendURL') + req.url)

    return res.redirect(config.get('backendURL') + req.url);

};


