const config = require('../../config')
const request = require('request')

module.exports = function () {
    let data = {
        administratorEmail: config.get('administratorEmail'),
        name: config.get('instanceUrl'),
        description: config.get('frontPageText')
    }
    
    let worUrl = req.body.webOfRegistries[-1] != "/" ? req.body.webOfRegistries : req.body.webOfRegistries.substring(0, -1);
    let updateSecret = config.get('webOfRegistriesSecret');
    let id = config.get('webOfRegistriesId');

    request.patch(worUrl + "/instances/" + id + "/", {
        json: data
    }, (err, resp, body) => {
        if (err) {
            console.log("Update error");
            console.log(err);
        } else {
            res.redirect('/admin/registries');
        }
    });
}
