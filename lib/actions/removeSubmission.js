var async = require('async');

var request = require('request')

var loadTemplate = require('../loadTemplate')

var config = require('../config')

module.exports = function(req, res) {

    var designId
    var uri

    if(req.params.userId) {
	designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	uri = config.get('databasePrefix') + 'user/' + encodeURIComponent(req.params.userId) + '/' + designId
    } else {
	designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	uri = config.get('databasePrefix') + 'public/' + designId
    } 
    console.log(uri)

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


