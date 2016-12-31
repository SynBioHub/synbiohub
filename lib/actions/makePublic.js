var pug = require('pug')

var async = require('async');

var request = require('request')

var getCollection = require('../get-sbol').getCollection

var serializeSBOL = require('../serializeSBOL')

var base64 = require('../base64')

var config = require('../config')

var loadTemplate = require('../loadTemplate')

module.exports = function(req, res) {

    var stack = require('../stack')()

    var uri = base64.decode(req.params.collectionURI)

    getCollection(null, uri, [ req.userStore ], function(err, sbol, collection) {

        var xml = serializeSBOL(sbol)

	async.series([
	    function convertSBOL(next) {
		request(
		    { method: 'POST',
		      uri: 'http://www.async.ece.utah.edu/validate/',
		      'content-type': 'application/json',
		      json: { 'options': {'language' : 'SBOL2',
					  'test_equality': false,
					  'check_uri_compliance': config.get('requireCompliant'),
					  'check_completeness': config.get('requireComplete'),
					  'check_best_practices': config.get('requireBestPractice'),
					  'continue_after_first_error': false,
					  'provide_detailed_stack_trace': false,
					  'subset_uri': '',
					  'uri_prefix': config.get('databasePrefix') + 'public/' + collection.displayId.replace('_collection','') + '/',
					  'version': '1',
					  'insert_type': false,
					  'main_file_name': 'main file',
					  'diff_file_name': 'comparison file',
					 },
			      'return_file': true,
			      'main_file': xml
			    }
		    }, function(err, response, body) {
			if(err || response.statusCode >= 300) {
			    console.log('err='+response.statusCode)
			    res.status(response.statusCode).send(err)
			} else {
			    if (!body.valid) {
				locals = {
				    errors: body.errors
				}
				res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
				return
			    }
 	   		    console.log(body.output_file);
			    request({
				method: 'GET',
 	   			uri: body.output_file,
			    },
				    function(err, response, body) {
					if(err || response.statusCode >= 300) {
					    console.log("err=" + err);
					    res.status(response.statusCode).send(err)
					}
					convertedSBOL = body
					next()
				    }
				   )	
			}
		    });
	    },
	
            function upload(next) {

		stack.getDefaultStore().upload(convertedSBOL, (err, result) => {

		    if(err) {
		    
			next(err)
		    
		    } else {
		    
			next()
		    }
		    
		})
	    },

            function removeSubmission(next) {

		var uri = base64.decode(req.params.collectionURI)
		uri = uri.substring(0,uri.lastIndexOf('/'))
		uri = uri.substring(0,uri.lastIndexOf('/'))

		var templateParams = {
		    uriPrefix: uri
		}

		var removeQuery = loadTemplate('sparql/remove.sparql', templateParams)

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
    })
};


