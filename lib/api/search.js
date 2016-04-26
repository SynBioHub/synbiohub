
var stack = require('../../lib/stack');

exports = module.exports = function(req, res) {

    var criteria = {};

    if(req.user)
        criteria.createdBy = req.user;

    triplestore.search(
        locals.triplestores, criteria, function(err, results) {

        if(err) {
            res.status(500).send(err);
            return;
        }

        res.status(200).send(results);
    });

};


