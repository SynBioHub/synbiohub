
var getComponentDefinition = require('../../lib/get-sbol').getComponentDefinition

var sbolmeta = require('sbolmeta')

var base64 = require('../base64')

var serializeSBOL = require('../serializeSBOL')

var request = require('request');

var SBOLDocument = require('sboljs')

module.exports = function(req, res) {

    var stack = require('../../lib/stack')()

    stack.getPrefixes((err, prefixes) => {

        var baseUri
        var uri

        if(req.params.designURI) {
            uri = base64.decode(req.params.designURI)
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

		var file = serializeSBOL(sbol)

		request(
		    { method: 'POST',
		      uri: 'http://sbolvalidator.zachzundel.com/endpoint.php',
		      'content-type': 'application/json',
		      body: JSON.stringify(
			  { validationOptions: {output : 'GenBank',
						test_equality: 'False',
						check_uri_compliance: 'False',
						check_completeness: 'False',
						check_best_practices: 'False',
						continue_after_first_error: 'False',
						provide_detailed_stack_trace: 'False',
						uri_prefix: '',
						version: '',
						subset_uri: '',
						insert_type: 'False',
						main_file_name: 'main file',
						diff_file_name: 'comparison file',
					       },
			    wantFileBack: 'True',
			    main_file: file
			  })
		    }, function(err, response, body) {

			if(err || response.statusCode >= 300) {
			    console.log('err='+response.statusCode)
			    res.status(response.statusCode).send(err)
			} else {
			    console.log('body='+body)
			    var genBank = body
			    res.header('content-type', 'text/plain').send(genBank)
			}
		    });
	    }
	})
    })
};


