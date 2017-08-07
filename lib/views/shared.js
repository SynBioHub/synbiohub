const pug = require('pug');
const config = require('../config');

module.exports = function (req, res) {
    let locals = {
        config: config.get(),
        section: 'shared',
        user: req.user
    };

    res.send(pug.renderFile('templates/views/shared.jade', locals));
};