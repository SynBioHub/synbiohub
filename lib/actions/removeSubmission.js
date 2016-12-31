var async = require('async');

var request = require('request')

var loadTemplate = require('../loadTemplate')

var base64 = require('../base64')

module.exports = function(req, res) {

    var uri = base64.decode(req.params.collectionURI)
    uri = uri.substring(0,uri.lastIndexOf('/'))
    uri = uri.substring(0,uri.lastIndexOf('/'))

    var templateParams = {
        uriPrefix: uri
    }

    var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

    async.series([

        function removeSubmission(next) {

            req.userStore.sparql(removeQuery, (err, result) => {

                if(err) {

                    next(err)

                } else {

		    res.redirect('/manage');
                }

            })
        }
	    
    ], function done(err) {

        res.status(500).send(err.stack)
                
    })
};


