
const config = require('./config')

const request = require('request')

function convertAndValidateSbol(xml, uriPrefix, version) {

    return new Promise((resolve, reject) => {

        request({
            method: 'POST',
            uri: config.get('converterURL'),
            'content-type': 'application/json',
            json: { 'options': {
                'language' : 'SBOL2',
                'test_equality': false,
                'check_uri_compliance': config.get('requireCompliant'),
                'check_completeness': config.get('requireComplete'),
                'check_best_practices': config.get('requireBestPractice'),
                'continue_after_first_error': false,
                'provide_detailed_stack_trace': false,
                'subset_uri': '',
                'uri_prefix': uriPrefix,
                'version': version,
                'insert_type': false,
                'main_file_name': 'main file',
                'diff_file_name': 'comparison file',
            },
                'return_file': true,
                'main_file': xml
            }
        }, function(err, response, body) {

            if(err || response.statusCode >= 300) {
                if (req.url==='/remoteSubmit') {
                    res.status(500).type('text/plain').send(err)			
                    return        
                } else {
                    reject(err || new Error('HTTP ' + response.statusCode))
                }
            } else {

                if(!body.valid) {

                    reject(new Error(JSON.stringify(body.errors)))

                } else {

                    const convertedSBOL = body.result

                    resolve(convertedSBOL)

                }
            }
        })

    })

}


