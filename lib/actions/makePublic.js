
var getCollection = require('../get-sbol').getCollection

var stack = require('../stack')

var serializeSBOL = require('../serializeSBOL')

var base64 = require('../base64')

module.exports = function(req, res) {

    var uri = base64.decode(req.params.collectionURI)

    getCollection(null, uri, [ req.userStore ], function(err, sbol, collection) {

        var xml = serializeSBOL(sbol)

        stack.getDefaultStore().upload(xml, (err, result) => {

            if(err) {

                res.status(500).send(err.stack)

            } else {

                res.redirect('/manage');
            }

        })

    })


};


