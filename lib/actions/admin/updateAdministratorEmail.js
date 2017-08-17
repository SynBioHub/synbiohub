
const config = require('../../config');
const updateWor = require('./updateWor');

module.exports = function(req, res) {

    if(req.body.administratorEmail) {
        config.set("administratorEmail", req.body.administratorEmail);

        res.redirect('/admin/registries');

        updateWor();
    }
}

