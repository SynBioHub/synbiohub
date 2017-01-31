
var loadTemplate = require('../loadTemplate')

var sparql = require('../sparql/sparql')

function UploadEndpoint(req, res, next) {

    console.log('endpoint: UploadEndpoint')

    sparql.upload(req.params.store, req.body, req.headers['content-type'], (err, msg) => {

        if(err) {
            return next(err)
        }

        res.status(200).send(msg)
    })
}

module.exports = UploadEndpoint


