var pug = require('pug')

var getComponentDefinition = require('../../lib/get-sbol').getComponentDefinition

var sbolmeta = require('sbolmeta')

var serializeSBOL = require('../serializeSBOL')

var request = require('request');

var SBOLDocument = require('sboljs')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function(req, res) {

	const { graphUris, uri, designId } = getUrisFromReq(req)

    getComponentDefinition(uri, graphUris).then((result) => {

        const sbol = result.sbol
        const componentDefinition = result.object

        var file = serializeSBOL(sbol)

        request(
            { method: 'POST',
                uri: config.get('converterURL'),
                'content-type': 'application/json',
                json: { 'options': {'language' : 'GenBank',
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
                }
            }, function(err, response, body) {
                if(err || response.statusCode >= 300) {

                    locals = {
                        section: 'errors',
                        user: req.user,
                        errors: [ err ]
                    }
                    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
                    return        

                } else {
                    if (!body.valid || body.errors != '') {
                        locals = {
                            errors: body.errors
                        }
                        res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
                        return
                    }
                    res.header('content-type', 'text/plain').send(body.result);
                }
            })

    }).catch((err) => {

        const locals = {
            section: 'errors',
            user: req.user,
            errors: [ uri + ' Not Found' ]
        }

        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })
};


