
const fs = require('fs')
const config = require('../../config')

module.exports = function(req, res) {
    config.set('instanceName', req.body['instanceName'])

    res.redirect('/admin/theme')
}

