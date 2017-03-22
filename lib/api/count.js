
const { getCount } = require('../query/type')

var pug = require('pug')

module.exports = function(req, res) {

    getCount(req.params.type, [null]).then((result) => {

        res.header('content-type', 'text/plain').send(result.toString())

    }).catch((err) => {

        locals = {
            section: 'errors',
            user: req.user,
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
        return        

    })

}
