
var getComponentDefinition = require('../../lib/get-sbol').getComponentDefinition

var sbolmeta = require('sbolmeta')

var base64 = require('../base64')

var serializeSBOL = require('../serializeSBOL')

var request = require('request');

var SBOLDocument = require('sboljs')

var config = require('../config')

module.exports = function(req, res) {

    var stack = require('../../lib/stack')()

    stack.getPrefixes((err, prefixes) => {

        var baseUri
        var uri

        if(req.params.designURI) {
            uri = base64.decode(req.params.designURI)
        } if (req.params.collectionId) {
	    var designId = req.params.collectionId + '/' + req.params.displayId + '/' + req.params.version
	    uri = config.get('databasePrefix') + 'public/' + designId
	} else {
            baseUri = prefixes[req.params.prefix]
            uri = baseUri + req.params.designid
        }

        var stores = [
            stack.getDefaultStore()
        ]

        if(req.userStore)
            stores.push(req.userStore)

        getComponentDefinition(null, uri, stores, function(err, sbol, componentDefinition) {

            if(err) {

                res.status(500).send(err)

            } else {

		var file = base64.encode(serializeSBOL(sbol))

		request(
		    { method: 'POST',
		      uri: 'http://www.async.ece.utah.edu/validate/',
		      'content-type': 'application/json',
		      body: JSON.stringify(
			  { 'options': {'language' : 'GenBank',
					'test_equality': false,
					'check_uri_compliance': false,
					'check_completeness': false,
					'check_best_practices': false,
					'continue_after_first_error': false,
					'provide_detailed_stack_trace': false,
					'subset_uri': '',
					'uri_prefix': '',
					'version': '',
					'insert_type': false,
					'main_file_name': 'main file',
					'diff_file_name': 'comparison file',
					       },
			    'return_file': true,
			    'main_file': file
			  })
		    }, function(err, response, body) {

			if(err || response.statusCode >= 300) {
			    console.log('err='+response.statusCode)
			    res.status(response.statusCode).send(err)
			} else {
			    console.log('err='+err)
			    console.log('response='+response.statusCode)
			    console.log('body='+body)
			    var genBank = body
			    res.header('content-type', 'text/plain').send(genBank)
			}
		    });
	    }
	})
    })
};


