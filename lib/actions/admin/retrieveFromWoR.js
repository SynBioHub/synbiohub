const config = require('../../config')
const request = require('request')

module.exports = function (req, res) {
    let worUrl = config.get('webOfRegistriesUrl');
    worUrl = worUrl[worUrl.length - 1] != "/" ? worUrl : worUrl.substring(0, worUrl.length - 1);

    request.get(worUrl + "/instances/", (err, resp, body) => {
        if (err) {
            console.log("Retrieval error");
            console.log(err);
        } else {
            let wor = config.get('webOfRegistries');
            let received = JSON.parse(body);

            received.forEach(registry => {
                wor[registry["uriPrefix"]] = registry["instanceUrl"];
            })

            config.set('webOfRegistries', wor);

            res.redirect('/admin/registries');
        }
    });
} 
