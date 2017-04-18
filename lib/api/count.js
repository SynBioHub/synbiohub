
const { getCount } = require('../query/count')

var pug = require('pug')

var config = require('../config')

module.exports = function(req, res) {

    getCount(req.params.type, null).then((result) => {

        res.header('content-type', 'text/plain').send(result.toString())

    }).catch((err) => {

        locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
        return        

    })

}
