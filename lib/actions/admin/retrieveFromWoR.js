const config = require('../../config')
const axios = require('axios')
const getRegistries = require('../../wor');

module.exports = function (req, res) {
    return getRegistries().then(() => res.redirect('/admin/registries'));
}
