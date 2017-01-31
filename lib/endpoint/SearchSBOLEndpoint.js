
var constructQuery = require('./constructQuery');

var loadTemplate = require('../loadTemplate')

var sparql = require('../sparql/sparql')

var config = require('../config')

var federate = require('../federation/federate')
var collateSBOL = require('../federation/collateSBOL')

function SearchSBOLEndpoint(type) {

    return federate(search, collateSBOL)

    function search(req, callback) {

        console.log('endpoint: SearchSBOLEndpoint')

        var params

        if(req.body) {
            params = req.body
        } else {
            params = req.query
        }

        var offset = ''
        var limit = ''

        if(params.offset !== undefined && params.offset !== null)
            offset = ' OFFSET ' + parseInt(params.offset);

        if(params.limit !== undefined && params.limit !== null)
            limit = ' LIMIT ' + parseInt(params.limit);

        var query = loadTemplate('./sparql/SearchSBOL.sparql', {
            type: type,
            criteria: constructQuery(type, params.criteria),
            offset: offset,
            limit: limit
        });

        sparql.query(query, req.params.store, 'application/rdf+xml', function(err, type, rdf) {

            if(err)
                return callback(err)

            callback(null, 200, {
                mimeType: type,
                body: rdf
            })
        });
    };
}

module.exports = SearchSBOLEndpoint;



