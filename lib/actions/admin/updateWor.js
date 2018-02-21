const config = require('../../config')
const request = require('request')

module.exports = function () {
    let data = {
        administratorEmail: config.get('administratorEmail'),
        name: config.get('instanceName'),
        description: config.get('frontPageText')
    }
    
    let worUrl = config.get('webOfRegistriesUrl');
    let updateSecret = config.get('webOfRegistriesSecret');
    let id = config.get('webOfRegistriesId');

    if(!(worUrl && updateSecret && id)) 
	return

    data.updateSecret = updateSecret;

    request.patch(worUrl + "/instances/" + id + "/", {
        json: data
    }, (err, resp, body) => {
        if (err) {
            console.log("Update error");
            console.log(err);
        } else {
            config.set('webOfRegistriesSecret', body["updateSecret"]);
        }
    });
}
